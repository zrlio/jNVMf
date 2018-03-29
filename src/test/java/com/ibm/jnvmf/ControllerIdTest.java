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

import org.junit.jupiter.api.Test;

class ControllerIdTest {

  @Test
  void constants() {
    ControllerId id = ControllerId.valueOf(ControllerId.ADMIN_DYNAMIC.toShort());
    assertEquals(ControllerId.ADMIN_DYNAMIC, id);
    id = ControllerId.valueOf(ControllerId.ADMIN_STATIC.toShort());
    assertEquals(ControllerId.ADMIN_STATIC, id);
  }

  @Test
  void equals() {
    ControllerId id = ControllerId.valueOf((short) 0x123);
    ControllerId id2 = ControllerId.valueOf((short) 0x123);
    assertEquals(id, id2);
  }
}