/*
 * Copyright (C) 2018, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ibm.jnvmf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ControllerTest {

  static NvmfTransportId getTransportId() {
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getDestinationAddress(),
        TestUtil.getPort());
    return new NvmfTransportId(socketAddress, TestUtil.getSubsystemNQN());
  }

  static Controller connectController() throws IOException {
    Nvme nvme = new Nvme();
    return nvme.connect(getTransportId());
  }


  @Tag("rdma")
  @Test
  void enableController() throws IOException {
    Controller controller = connectController();
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    assertFalse(configuration.getEnable());
    configuration.setEnable(true);
    controller.syncConfiguration();
    assertTrue(configuration.getEnable());
  }

  @Tag("rdma")
  @Test
  void keepAlive() throws IOException, InterruptedException {
    Controller controller = connectController();
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setEnable(true);
    controller.syncConfiguration();
    /* NVMe Spec 1.3a - 5.21.1.15 default value for Fabrics is 2 min */
    long end = System.nanoTime() + TimeUnit.NANOSECONDS.convert(3, TimeUnit.MINUTES);
    while (end < System.nanoTime()) {
      controller.keepAlive();
      /* send keep alive every 10s */
      Thread.sleep(10000);
    }
    /* controller should still be responsive after 3 minutes */
    controller.syncConfiguration();
    Thread.sleep(TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES));
    /* controller should have timed out */
    assertThrows(IOException.class, () -> controller.syncConfiguration());
  }

  @Tag("rdma")
  @Test
  void freeController() throws IOException {
    Controller controller = connectController();
    assertTrue(controller.isValid());
    controller.free();
    assertFalse(controller.isValid());
  }

  @Tag("rdma")
  @Test
  void controllerStatus() throws IOException, TimeoutException {
    Controller controller = connectController();
    ControllerStatus status = controller.getControllerStatus();
    assertFalse(status.isReady());
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
    status = controller.getControllerStatus();
    assertTrue(status.isReady());
  }

  @Tag("rdma")
  @Test
  void shutdown() throws IOException {
    Controller controller = connectController();
    ControllerStatus status = controller.getControllerStatus();
    assertEquals(ControllerStatus.ShutdownStatus.getInstance().NORMAL_OPERATION,
        status.getShutdownStatus());
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setShutdownNotification(
        ControllerConfiguration.ShutdownNotification.getInstance().NORMAL_SHUTDOWN);
    controller.syncConfiguration();
    assertEquals(ControllerConfiguration.ShutdownNotification.getInstance().NORMAL_SHUTDOWN,
        configuration.getShutdownNotification());
    /* NVMf Spec 1.0 - 4.5 the controller should not initiate a disconnect at the NVMe Transport level */
    status = controller.getControllerStatus();
    assertTrue(ControllerStatus.ShutdownStatus.getInstance().SHUTDOWN_PROCESSING
        .equals(status.getShutdownStatus()) ||
        ControllerStatus.ShutdownStatus.getInstance().SHUTDOWN_COMPLETE
            .equals(status.getShutdownStatus()));
  }

  @Tag("rdma")
  @Test
  void controllerCapabilties() throws IOException {
    Controller controller = connectController();
    ControllerCapabilities controllerCapabilities = controller.getControllerCapabilities();
    int mpsmax = controllerCapabilities.getMemoryPageSizeMaximum().toInt();
    int mpsmin = controllerCapabilities.getMemoryPageSizeMinimum().toInt();
    ControllerConfiguration controllerConfiguration = controller.getControllerConfiguration();
    int mps = controllerConfiguration.getMemoryPageSize().toInt();
    assertTrue(mps >= mpsmin);
    assertTrue(mps <= mpsmax);

    /* Note: CAP.CQR shall be set to fixed value 1h. */
    assertEquals(true, controllerCapabilities.getContiguousQueuesRequired());
  }

  @Tag("rdma")
  @Test
  void controllerIdentify() throws IOException, TimeoutException {
    Controller controller = connectController();
    assertThrows(UnsuccessfulComandException.class, () -> controller.getIdentifyControllerData());
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
    IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
    assertEquals(controller.getControllerId(), identifyControllerData.getControllerId());
  }

  @Tag("rdma")
  @Test
  void createIOQueue() throws IOException, TimeoutException {
    {
      Controller controller = connectController();
      assertThrows(IllegalStateException.class, () -> controller.createIoQueuePair(32));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      IoQueuePair ioQueuePair = controller.createIoQueuePair(32);
      assertEquals(32, ioQueuePair.getSubmissionQueueSize());
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class, () -> controller.createIoQueuePair(0));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class, () -> controller.createIoQueuePair(-1));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class,
          () -> controller.createIoQueuePair(32, -1, 0, 0));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class,
          () -> controller.createIoQueuePair(32, 0, -1, 0));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class,
          () -> controller.createIoQueuePair(32, 0, 0, -1));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class,
          () -> controller.createIoQueuePair(32, Integer.MAX_VALUE, 0, 0));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IllegalArgumentException.class,
          () -> controller.createIoQueuePair(32, 0, Integer.MAX_VALUE, 0));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      assertThrows(IOException.class,
          () -> controller.createIoQueuePair(32, 0, 0, 1024*1024));
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      IoQueuePair ioQueuePair = controller.createIoQueuePair(32, 0, 0, 0);
      assertEquals(32, ioQueuePair.getSubmissionQueueSize());
      assertEquals(0, ioQueuePair.getMaximumAdditionalSgls());
      assertEquals(0, ioQueuePair.getInCapsuleDataSize());
      assertEquals(0, ioQueuePair.getInlineDataSize());
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      IoQueuePair ioQueuePair = controller.createIoQueuePair(32, 0, 512, 0);
      assertEquals(32, ioQueuePair.getSubmissionQueueSize());
      assertEquals(0, ioQueuePair.getMaximumAdditionalSgls());
      assertEquals(512, ioQueuePair.getInCapsuleDataSize());
      assertEquals(0, ioQueuePair.getInlineDataSize());
      controller.free();
    }
    {
      Controller controller = connectController();
      controller.getControllerConfiguration().setEnable(true);
      controller.syncConfiguration();
      controller.waitUntilReady();
      IoQueuePair ioQueuePair = controller.createIoQueuePair(32, 0, 0, 64);
      assertEquals(32, ioQueuePair.getSubmissionQueueSize());
      assertEquals(0, ioQueuePair.getMaximumAdditionalSgls());
      assertEquals(0, ioQueuePair.getInCapsuleDataSize());
      assertEquals(64, ioQueuePair.getInlineDataSize());
      controller.free();
    }
  }

  @Tag("rdma")
  @Test
  void activeNamespaces() throws IOException, TimeoutException {
    Controller controller = connectController();
    assertThrows(UnsuccessfulComandException.class, () -> controller.getActiveNamespaces());
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
    List<Namespace> namespaces = controller.getActiveNamespaces();
    /* assume single active namespace with id == 1 */
    assertEquals(1, namespaces.size());
    Namespace namespace = namespaces.get(0);
    assertEquals(new NamespaceIdentifier(1), namespace.getIdentifier());
  }

  @Tag("rdma")
  @Test
  void transportId() throws IOException {
    Controller controller = connectController();
    assertEquals(getTransportId(), controller.getTransportId());
  }

  @Tag("rdma")
  @Test
  void controllerId() throws IOException {
    Controller controller = connectController();
    assertNotEquals(ControllerId.ADMIN_DYNAMIC, controller.getControllerId());
    assertNotEquals(ControllerId.ADMIN_STATIC, controller.getControllerId());
  }
}