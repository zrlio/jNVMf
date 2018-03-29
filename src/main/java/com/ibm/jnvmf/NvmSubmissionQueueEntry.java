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

public abstract class NvmSubmissionQueueEntry extends AdminSubmissionQeueueEntry {

  private static final int FUSED_OPERATION_OFFSET = 1;

  NvmSubmissionQueueEntry(NativeBuffer buffer) {
    super(buffer);
  }

  public void setFusedOperation(NvmFusedOperation.Value fusedOperation) {
    int sndByte = getBuffer().get(FUSED_OPERATION_OFFSET);
    sndByte = sndByte | fusedOperation.toInt();
    getBuffer().put(FUSED_OPERATION_OFFSET, (byte) sndByte);
  }


  @Override
  public void setNamespaceIdentifier(NamespaceIdentifier namespaceIdentifier) {
    /* All NVM commands use the namespace identifier field */
    super.setNamespaceIdentifier(namespaceIdentifier);
  }

  @Override
  void initialize() {
    super.initialize();
    setFusedOperation(NvmFusedOperation.getInstance().NORMAL);
  }
}
