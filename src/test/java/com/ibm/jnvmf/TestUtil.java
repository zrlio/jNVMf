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

public class TestUtil {

  final static String getLocalAddress() {
    String address = System.getProperty("localAddress");
    if (address == null) {
      throw new IllegalArgumentException("set -DlocalAddress");
    }
    return address;
  }

  final static String getDestinationAddress() {
    String address = System.getProperty("destinationAddress");
    if (address == null) {
      throw new IllegalArgumentException("set -DdestinationAddress");
    }
    return address;
  }

  final static int getPort() {
    String port = System.getProperty("port");
    if (port != null) {
      return Integer.parseInt(port);
    } else {
      return 50025;
    }
  }

  final static NvmeQualifiedName getSubsystemNQN() {
    String nqn = System.getProperty("nqn");
    if (nqn != null) {
      return new NvmeQualifiedName(nqn);
    } else {
      return new NvmeQualifiedName("nqn.2016-06.io.spdk:cnode1");
    }
  }
}
