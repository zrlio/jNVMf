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

public abstract class FabricsSubmissionQueueEntry extends SubmissionQueueEntry {
  /*
   * NVMf Spec 1.0 - 2.1
   *
   *  04      Fabrics CommandCapsule Type
   *  02:03   CommandCapsule Identifier (CID) - unique identifier together with SQ identifier
   *  01      Reserved
   *  0       CommandType (OPC)
   *
   */

  private static final int FABRIC_COMMAND_TYPE_OFFSET = 4;

  FabricsSubmissionQueueEntry(NativeBuffer buffer) {
    super(buffer);
  }

  private final void setFabricsCommandType(FabricsCommandType commandType) {
    getBuffer().put(FABRIC_COMMAND_TYPE_OFFSET, commandType.toByte());
  }

  abstract FabricsCommandType getCommandType();


  @Override
  void initialize() {
    super.initialize();
    setOpcode(FabricsCommandOpcode.FABRIC);
    setFabricsCommandType(getCommandType());
  }
}
