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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Nvme {

  private final NvmeQualifiedName hostNvmeQualifiedName;

  public Nvme(NvmeQualifiedName hostNvmeQualifiedName) {
    if (hostNvmeQualifiedName == null) {
      throw new IllegalArgumentException("Host NQN null");
    }
    this.hostNvmeQualifiedName = hostNvmeQualifiedName;
  }

  public Nvme() throws UnknownHostException {
    // TODO: use UUID
    this(new NvmeQualifiedName("nqn.2014-08.com.example:"
        + InetAddress.getLocalHost().getCanonicalHostName()));
  }

  public Controller connect(NvmfTransportId transportId) throws IOException {
    return new Controller(hostNvmeQualifiedName, transportId);
  }

  public NvmeQualifiedName getHostNvmeQualifiedName() {
    return hostNvmeQualifiedName;
  }
}
