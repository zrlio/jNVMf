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

abstract class CompletionQueueEntry extends Updatable<NativeBuffer> {
  /*
   * NVMf Spec 1.0 - 2.2 and NVMe Spec 1.3a - 4.6.1
   *
   * 07:00 Command specific
   * 09:08 SQ head pointer
   * 11:10 Reserved
   * 13:12 Command Identifier
   * 15:14 Status:
   *  31 Do not retry
   *  30 More
   *  28:29 Reserved
   *  25:27 Status code type
   *  17:24 Status code
   *
   */

  public static final int SIZE = 16;

  private static final int SUBMISSION_QUEUE_HEAD_POINTER_OFFSET = 8;
  private short submissionQueueHeadPointer;

  private static final int COMMAND_IDENTIFIER_OFFSET = 12;
  private short commandIdentifier;

  private static final int STATUS_CODE_OFFSET = 14;

  private static final int STATUS_CODE_TYPE_OFFSET = 15;
  private StatusCodeType.Value statusCodeType;

  private static final int MORE_OFFSET = 15;
  private static final int MORE_BITOFFSET = 6;
  private boolean more;

  private static final int DO_NOT_RETRY_OFFSET = 15;
  private static final int DO_NOT_RETRY_BITOFFSET = 7;
  private boolean doNotRetry;


  public final short getSubmissionQueueHeadPointer() {
    return submissionQueueHeadPointer;
  }

  public final short getCommandIdentifier() {
    return commandIdentifier;
  }

  static short getCommandIdentifier(NativeBuffer buffer) {
    return buffer.getShort(COMMAND_IDENTIFIER_OFFSET);
  }

  int getStatusCodeRaw(NativeBuffer buffer) {
    int raw = buffer.getShort(STATUS_CODE_OFFSET);
    return BitUtil.getBits(raw, 1, 8);
  }

  public abstract StatusCode.Value getStatusCode();

  public final StatusCodeType.Value getStatusCodeType() {
    return statusCodeType;
  }

  public final boolean getMore() {
    return more;
  }

  public final boolean getDoNotRetry() {
    return doNotRetry;
  }

  @Override
  void update(NativeBuffer buffer) {
    /* According to NVMf Spec 1.0 - 1.3 conventions */
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    submissionQueueHeadPointer = buffer.getShort(SUBMISSION_QUEUE_HEAD_POINTER_OFFSET);
    commandIdentifier = getCommandIdentifier(buffer);
    int b1 = buffer.get(STATUS_CODE_TYPE_OFFSET);
    statusCodeType = StatusCodeType.getInstance().valueOf(BitUtil.getBits(b1, 1, 3));
    assert (MORE_OFFSET == STATUS_CODE_TYPE_OFFSET);
    more = BitUtil.getBit(b1, MORE_BITOFFSET);
    assert (DO_NOT_RETRY_OFFSET == STATUS_CODE_TYPE_OFFSET);
    doNotRetry = BitUtil.getBit(b1, DO_NOT_RETRY_BITOFFSET);
  }
}
