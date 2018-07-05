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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NvmfRdmaEndpointTest {

  private Process process;
  private static final int RPING_DEFAULT_PORT = 7174;
  private static final int DEFAULT_CONNECT_TIMEOUT = 10000; /* ms */
  private NvmfRdmaEndpointGroup endpointGroup;

  NvmfRdmaEndpointTest() throws IOException {
    endpointGroup = new NvmfRdmaEndpointGroup(2, TimeUnit.SECONDS);
    endpointGroup.init(new NvmfRdmaEndpointFactory(endpointGroup));
  }

  @BeforeAll
  void startRping() throws IOException {
    process = new ProcessBuilder("rping", "-s", "-P").start();
  }

  @AfterAll
  void endRping() throws IOException {
    process.destroyForcibly();
  }

  NvmfRdmaEndpoint connect(NvmfRdmaEndpointGroup endpointGroup, int port) throws Exception {
    NvmfRdmaEndpoint endpoint = endpointGroup.createEndpoint();
    endpoint.setCqSize(128);
    endpoint.setRqSize(64);
    endpoint.setSqSize(64);
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getLocalAddress(), port);
    endpoint.connect(socketAddress, 10000);
    return endpoint;
  }

  @Tag("rdma")
  @Test
  void connectTest() throws Exception {
    connect(endpointGroup, RPING_DEFAULT_PORT);
  }

  @Tag("rdma")
  @Test
  void checkArguments() throws Exception {
    NvmfRdmaEndpointGroup endpointGroup = new NvmfRdmaEndpointGroup(2, TimeUnit.SECONDS);
    endpointGroup.init(new NvmfRdmaEndpointFactory(endpointGroup));
    NvmfRdmaEndpoint endpoint = endpointGroup.createEndpoint();
    assertThrows(IllegalArgumentException.class, () -> endpoint.setSqSize(0));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setSqSize(-1));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setRqSize(0));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setRqSize(-1));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setCqSize(0));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setCqSize(-1));
    assertThrows(IllegalArgumentException.class, () -> endpoint.setInlineDataSize(-1));
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getLocalAddress(), 7174);
    assertThrows(IllegalArgumentException.class,
        () -> endpoint.connect(socketAddress, 10000));
  }

  @Tag("rdma")
  @Test
  void createQP() throws Exception {
    NvmfRdmaEndpointGroup endpointGroup = new NvmfRdmaEndpointGroup(2, TimeUnit.SECONDS);
    endpointGroup.init(new NvmfRdmaEndpointFactory(endpointGroup));
    NvmfRdmaEndpoint endpoint = endpointGroup.createEndpoint();
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getLocalAddress(), 7174);
    endpoint.setSqSize(Integer.MAX_VALUE);
    endpoint.setRqSize(16);
    endpoint.setCqSize(16);
    assertThrows(IOException.class,
        () -> endpoint.connect(socketAddress, DEFAULT_CONNECT_TIMEOUT));
    endpoint.setSqSize(16);
    endpoint.setRqSize(Integer.MAX_VALUE);
    endpoint.setCqSize(16);
    assertThrows(IOException.class,
        () -> endpoint.connect(socketAddress, DEFAULT_CONNECT_TIMEOUT));
    endpoint.setSqSize(16);
    endpoint.setRqSize(16);
    endpoint.setCqSize(Integer.MAX_VALUE);
    assertThrows(IOException.class,
        () -> endpoint.connect(socketAddress, DEFAULT_CONNECT_TIMEOUT));
    endpoint.setSqSize(16);
    endpoint.setRqSize(16);
    endpoint.setCqSize(16);
    endpoint.setInlineDataSize(Integer.MAX_VALUE);
    assertThrows(IOException.class,
        () -> endpoint.connect(socketAddress, DEFAULT_CONNECT_TIMEOUT));
  }

  @Tag("rdma")
  @Test
  void bufferPool() throws Exception {
    NvmfRdmaEndpoint endpoint = connect(endpointGroup, RPING_DEFAULT_PORT);
    KeyedNativeBufferPool bufferPool = endpoint.getBufferPool(512);
    KeyedNativeBufferPool bufferPool2 = endpoint.getBufferPool(512);
    assertEquals(bufferPool, bufferPool2);
    KeyedNativeBufferPool bufferPool3 = endpoint.getBufferPool(513);
    assertNotEquals(bufferPool, bufferPool3);

    KeyedNativeBuffer buffer = bufferPool.allocate();
    assertEquals(512, buffer.capacity());

    int port = 1234;
    Process process = new ProcessBuilder("rping", "-s", "-P", "-p", Integer.toString(port)).start();
    NvmfRdmaEndpoint endpoint2 = connect(endpointGroup, port);
    KeyedNativeBufferPool bufferPool4 = endpoint2.getBufferPool(512);
    assertEquals(bufferPool, bufferPool4);

    NvmfRdmaEndpointGroup endpointGroup1 = new NvmfRdmaEndpointGroup(2, TimeUnit.SECONDS);
    endpointGroup1.init(new NvmfRdmaEndpointFactory(endpointGroup1));
    NvmfRdmaEndpoint endpoint3 = connect(endpointGroup1, port);
    KeyedNativeBufferPool bufferPool5 = endpoint3.getBufferPool(512);
    assertNotEquals(bufferPool, bufferPool5);
    process.destroyForcibly();
  }
}