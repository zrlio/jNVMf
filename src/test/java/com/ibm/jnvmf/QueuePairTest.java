package com.ibm.jnvmf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class QueuePairTest {

  static Controller connectController() throws IOException, TimeoutException {
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getDestinationAddress(),
        TestUtil.getPort());
    NvmfTransportId tid = new NvmfTransportId(socketAddress, TestUtil.getSubsystemNQN());
    Nvme nvme = new Nvme();
    Controller controller = nvme.connect(tid);
    ControllerConfiguration configuration = controller.getControllerConfiguration();
    configuration.setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
    return controller;
  }

  @Tag("rdma")
  @Test
  public void registerMemory() throws IOException, TimeoutException {
    IoQueuePair ioQueuePair = connectController().createIoQueuePair(32);
    {
      ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
      buffer.position(5);
      buffer.limit(678);
      KeyedNativeBuffer keyedNativeBuffer = ioQueuePair.registerMemory(buffer);
      assertEquals(buffer.position(), keyedNativeBuffer.position());
      assertEquals(buffer.limit(), keyedNativeBuffer.limit());
      keyedNativeBuffer.free();
      assertFalse(keyedNativeBuffer.isValid());
    }

    {
      ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
      assertThrows(ClassCastException.class, () -> ioQueuePair.registerMemory(buffer));
    }
  }

  @Tag("rdma")
  @Test
  public void inlineDataSize() throws IOException, TimeoutException {
    IoQueuePair ioQueuePair = connectController().createIoQueuePair(32, 0, 0,64);
    assertTrue(ioQueuePair.getInlineDataSize() >= 64);
  }

  @Tag("rdma")
  @Test
  public void getController() throws IOException, TimeoutException {
    Controller controller = connectController();
    IoQueuePair ioQueuePair = controller.createIoQueuePair(32);
    assertEquals(controller, ioQueuePair.getController());
  }

  @Tag("rdma")
  @Test
  public void free() throws IOException, TimeoutException {
    IoQueuePair ioQueuePair = connectController().createIoQueuePair(32);
    assertTrue(ioQueuePair.isValid());
    ioQueuePair.free();
    assertFalse(ioQueuePair.isValid());
    ioQueuePair.free();
  }

  @Tag("rdma")
  @Test
  public void getSubmissionQueueSize() throws IOException, TimeoutException {
    IoQueuePair ioQueuePair = connectController().createIoQueuePair(32);
    assertEquals(32, ioQueuePair.getSubmissionQueueSize());
  }
}