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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NativeNvmeQualifiedNameTest {

  private NativeBuffer buffer;


  @BeforeEach
  void init() {
    buffer = new NativeByteBuffer(ByteBuffer.allocateDirect(NativeNvmeQualifiedName.SIZE));
  }

  @Test
  void idempotent() {
    String nqn = "nqn.2014-08.com.example:nvme:nvm-subsystem-sn-d78432";
    NativeNvmeQualifiedName nativeNQN = new NativeNvmeQualifiedName(buffer);
    nativeNQN.set(new NvmeQualifiedName(nqn));
    String decodeNqn = nativeNQN.get().toString();
    assertEquals(nqn.length(), decodeNqn.length());
    for (int i = 0; i < Math.min(nqn.length(), decodeNqn.length()); i++) {
      if (nqn.charAt(i) != decodeNqn.charAt(i)) {
        assertTrue(false, "Expected: " + Integer.toHexString(nqn.charAt(i)) + ", but was: " +
            Integer.toHexString(decodeNqn.charAt(i)));
      }
    }
  }

  @Test
  void stringToLarge() {
    StringBuilder str = new StringBuilder("nqn.2014-08.com.example:nvme:nvm-subsystem-sn-d78432");
    for (int i = str.length(); i < NativeNvmeQualifiedName.SIZE + 2; i++) {
      str.append('a');
    }
    NvmeQualifiedName nqn = new NvmeQualifiedName(str.toString());
    NativeNvmeQualifiedName nativeNQN = new NativeNvmeQualifiedName(buffer);
    assertThrows(IllegalArgumentException.class, () -> nativeNQN.set(nqn));
  }
}