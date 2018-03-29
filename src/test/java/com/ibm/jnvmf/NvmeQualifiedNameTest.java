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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NvmeQualifiedNameTest {
  /*
   * NVMe Spec 1.3a - 7.9
   */

  @Test
  void namingAuthority() {
    String nqn = "nqn.2014-08.com.example:nvme:nvm-subsystem-sn-d78432";
    new NvmeQualifiedName(nqn);
    nqn = "nqn.2014-08.com.example:nvme.host.sys.xyz";
    new NvmeQualifiedName(nqn);
  }

  @Test
  void uniqueIdentifier() {
    String nqn = "nqn.2014-08.org.nvmexpress:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
    new NvmeQualifiedName(nqn);
  }

  @Test
  void invalidNQN() {
    String nqn = "nqn.x";
    assertThrows(IllegalArgumentException.class, () -> new NvmeQualifiedName(nqn));
  }
}