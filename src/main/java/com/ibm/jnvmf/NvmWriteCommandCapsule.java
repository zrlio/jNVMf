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

public class NvmWriteCommandCapsule extends NvmIoCommandCapsule {

  private static final SubmissionQueueEntryFactory<NvmIoCommandSqe> sqeFactory =
      buffer -> new NvmWriteCommandSqe(buffer);

  NvmWriteCommandCapsule(KeyedNativeBuffer buffer, int additionalSgls, int inCapsuleDataOffset,
      int inCapsuleDataSize) {
    super(buffer, sqeFactory, additionalSgls, inCapsuleDataOffset, inCapsuleDataSize);

  }

  @Override
  public NvmWriteCommandSqe getSubmissionQueueEntry() {
    return (NvmWriteCommandSqe) super.getSubmissionQueueEntry();
  }


  void setIncapsuleData(NativeBuffer incapsuleData) {
    SglDataBlockDescriptor sglDataBlockDescriptor = getSubmissionQueueEntry()
        .getSglDataBlockDescriptor();
    sglDataBlockDescriptor.setOffset(getIncapsuleDataOffset() + incapsuleData.position());
    sglDataBlockDescriptor.setLength(incapsuleData.remaining());
  }
}
