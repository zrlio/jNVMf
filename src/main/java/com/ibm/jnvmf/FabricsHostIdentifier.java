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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

final class FabricsHostIdentifier {

  /*
   * NVMe Spec 1.3a - 5.21.1.19.2
   * The Host Identifier shall be an extended 128-bit Host Identifier
   */
  public static final int SIZE = 16;

  private byte[] identifier;

  private FabricsHostIdentifier() {
  }

  public void get(NativeBuffer buffer) {
    if (buffer.remaining() < SIZE) {
      throw new IllegalArgumentException("buffer to small");
    }
    if (identifier == null) {
      try {
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface
            .getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements() && identifier == null) {
          NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
          identifier = networkInterface.getHardwareAddress();
        }
        if (identifier == null) {
          throw new SocketException();
        }
      } catch (SocketException exception) {
        System.err.println("WARN: Could not get MAC address for host identifier");
        exception.printStackTrace();
        identifier = new byte[]{0x12, 0x34, 0x56, 0x78};
      }
    }
    buffer.put(identifier);
  }

  private static FabricsHostIdentifier fabricsHostIdentifier;

  static FabricsHostIdentifier getInstance() {
    if (fabricsHostIdentifier == null) {
      fabricsHostIdentifier = new FabricsHostIdentifier();
    }
    return fabricsHostIdentifier;
  }
}
