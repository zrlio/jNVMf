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

public class AdminIdentifyCommandSqe extends AdminSubmissionQeueueEntry {
  /*
   * NVMe Spec 1.3a - 5.15
   *
   * Dword10:
   * 31:16 Controller identifier
   * 15:08 Reserved
   * 00:07 Controller or namepsace structure
   */

  private static final int CONTROLLER_NAMESPACE_STRUCTURE_OFFSET = 40;

  private final KeyedSglDataBlockDescriptor keyedSglDataBlockDescriptor;

  AdminIdentifyCommandSqe(NativeBuffer buffer) {
    super(buffer);
    this.keyedSglDataBlockDescriptor = new KeyedSglDataBlockDescriptor(getSglDescriptor1Buffer());
  }

  void setReturnType(AdminIdentifyCommandReturnType.Value returnType) {
    getBuffer().put(CONTROLLER_NAMESPACE_STRUCTURE_OFFSET, returnType.toByte());
  }

  /* we don't support namespace management and virtualization enhancements at the moment */
  /*void setControllerId(ControllerId controllerId) {
   }*/

  @Override
  void initialize() {
    super.initialize();
    setOpcode(AdminCommandOpcode.IDENTIFY);
  }

  KeyedSglDataBlockDescriptor getKeyedSglDataBlockDescriptor() {
    return keyedSglDataBlockDescriptor;
  }
}
