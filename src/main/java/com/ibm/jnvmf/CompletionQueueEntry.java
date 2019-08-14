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

import java.nio.ByteOrder;

abstract class CompletionQueueEntry extends StatusField {
  /*
   * NVMf Spec 1.0 - 2.2 and NVMe Spec 1.3a - 4.6.1
   *
   * 07:00 Command specific
   * 09:08 SQ head pointer
   * 11:10 Reserved
   * 13:12 Command Identifier
   * 15:14 Status Field
   *
   */

  public static final int SIZE = 16;

  private static final int SUBMISSION_QUEUE_HEAD_POINTER_OFFSET = 8;
  private short submissionQueueHeadPointer;

  private static final int COMMAND_IDENTIFIER_OFFSET = 12;
  private short commandIdentifier;

  private static final int STATUS_FIELD_OFFSET = 14;

  CompletionQueueEntry() {
    super(STATUS_FIELD_OFFSET);
  }

  public final short getSubmissionQueueHeadPointer() {
    return submissionQueueHeadPointer;
  }

  public final short getCommandIdentifier() {
    return commandIdentifier;
  }

  static short getCommandIdentifier(NativeBuffer buffer) {
    return buffer.getShort(COMMAND_IDENTIFIER_OFFSET);
  }

  public abstract StatusCode.Value getStatusCode();

  @Override
  void update(NativeBuffer buffer) {
    /* According to NVMf Spec 1.0 - 1.3 conventions */
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    submissionQueueHeadPointer = buffer.getShort(SUBMISSION_QUEUE_HEAD_POINTER_OFFSET);
    commandIdentifier = getCommandIdentifier(buffer);
    super.update(buffer);
  }
}
