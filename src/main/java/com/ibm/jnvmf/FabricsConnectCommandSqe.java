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

import java.util.concurrent.TimeUnit;

public final class FabricsConnectCommandSqe extends FabricsSubmissionQueueEntry {

  /*
   * NVMf Spec 1.0 - 3.3
   *
   * 39:24    SGL Descriptor
   * 41:40    Record format - 0x0 in this definition
   * 42:43    QueueId
   * 45:44    Submission Queue Size
   * 46       Connect Attributes -> 00b for Admin Queues
   * 47       Reserved
   * 48:51    Keep Alive Timeout
   * 52:63    Reserved
   *
   */

  /* Record format by specification 0 */
  private static final int RECORD_FORMAT_OFFSET = 40;
  private static final int QUEUE_ID_OFFSET = 42;
  private static final int SUBMISSION_QUEUE_SIZE_OFFSET = 44;
  /* Connection attribute is only relevant for weighted round robin - we don't support it for now */
  private static final int CONNECT_ATTRIBUTES_OFFSET = 46;
  private static final int KEEP_ALIVE_TIMEOUT_OFFSET = 48;

  private final KeyedSglDataBlockDescriptor keyedSglDataBlockDescriptor;

  FabricsConnectCommandSqe(NativeBuffer buffer) {
    super(buffer);
    this.keyedSglDataBlockDescriptor = new KeyedSglDataBlockDescriptor(getSglDescriptor1Buffer());
  }

  KeyedSglDataBlockDescriptor getKeyedSglDataBlockDescriptor() {
    return keyedSglDataBlockDescriptor;
  }

  public void setQueueId(QueueId queueId) {
    getBuffer().putShort(QUEUE_ID_OFFSET, queueId.toShort());
  }

  public void setSubmissionQueueSize(short submissionQueueSize) {
    getBuffer().putShort(SUBMISSION_QUEUE_SIZE_OFFSET, submissionQueueSize);
  }

  public void setKeepAliveTimeout(long duration, TimeUnit unit) {
    long millis = TimeUnit.MILLISECONDS.convert(duration, unit);
    /* toIntExact will not convert if millis > 2^31-1 since it is converting to a signed int */
    getBuffer().putInt(KEEP_ALIVE_TIMEOUT_OFFSET, Math.toIntExact(millis));
  }

  @Override
  FabricsCommandType getCommandType() {
    return FabricsCommandType.CONNECT;
  }

  @Override
  void initialize() {
    super.initialize();
    /* NVMe Spec 1.3a - 5.21.1.15 default value for Fabrics is 2 min */
    setKeepAliveTimeout(2, TimeUnit.MINUTES);
  }
}
