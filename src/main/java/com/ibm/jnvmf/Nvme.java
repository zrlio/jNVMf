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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Nvme {
  private static final long CONTROLLER_CONNECT_TIMEOUT = 1;
  private static final TimeUnit CONTROLLER_CONNTECT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private final NvmeQualifiedName hostNvmeQualifiedName;

  public Nvme(NvmeQualifiedName hostNvmeQualifiedName) {
    if (hostNvmeQualifiedName == null) {
      throw new IllegalArgumentException("Host NQN null");
    }
    this.hostNvmeQualifiedName = hostNvmeQualifiedName;
  }

  public Nvme() {
    this(new NvmeQualifiedName(UUID.randomUUID()));
  }

  public Controller connect(NvmfTransportId transportId) throws IOException {
    return connect(transportId, CONTROLLER_CONNECT_TIMEOUT, CONTROLLER_CONNTECT_TIMEOUT_UNIT,
        true);
  }

  public Controller connect(NvmfTransportId transportId, long connectTimeout,
      TimeUnit connectTimeoutUnit, boolean dynamicId) throws IOException {
    return new Controller(hostNvmeQualifiedName, transportId, connectTimeout, connectTimeoutUnit,
        dynamicId);
  }

  public NvmeQualifiedName getHostNvmeQualifiedName() {
    return hostNvmeQualifiedName;
  }
}
