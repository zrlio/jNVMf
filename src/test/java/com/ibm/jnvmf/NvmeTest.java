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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class NvmeTest {

  @Test
  void hostNQN() {
    Nvme nvme = new Nvme();
    NvmeQualifiedName uuidNqn = nvme.getHostNvmeQualifiedName();
    assertTrue(uuidNqn.toString().startsWith("nqn.2014-08.org.nvmexpress:uuid:"));

    String nqn = "nqn.2014-08.com.example:nvme.host.sys.xyz";
    nvme = new Nvme(new NvmeQualifiedName(nqn));
    assertEquals(nqn, nvme.getHostNvmeQualifiedName().toString());
  }

  @Tag("rdma")
  @Test
  void connectTest() throws IOException {
    /* assume target is started */
    Nvme nvme = new Nvme();
    InetSocketAddress socketAddress = new InetSocketAddress(TestUtil.getDestinationAddress(),
        TestUtil.getPort());
    NvmfTransportId transportId = new NvmfTransportId(socketAddress, TestUtil.getSubsystemNQN());
    assertTrue(nvme.connect(transportId) != null);
    assertTrue(nvme.connect(transportId, 5000, TimeUnit.MILLISECONDS, true) != null);
  }
}