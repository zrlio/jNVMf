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

public class RdmaCmRequestPrivateData extends NativeData<NativeBuffer> {

  static final int SIZE = 32;
  private static final int RECORD_FORMAT_OFFSET = 0;
  private static final int QUEUE_ID_OFFSET = 2;
  private static final int RDMA_QP_RECEIVE_QUEUE_SIZE_OFFSET = 4;
  private static final int RDMA_QP_SEND_QUEUE_SIZE_OFFSET = 6;

  RdmaCmRequestPrivateData(NativeBuffer buffer) {
    super(buffer, SIZE);
  }

  void setQueueId(QueueId queueId) {
    getBuffer().putShort(QUEUE_ID_OFFSET, queueId.toShort());
  }

  void setRdmaQpReceiveQueueSize(short size) {
    getBuffer().putShort(RDMA_QP_RECEIVE_QUEUE_SIZE_OFFSET, size);
  }

  void setRdmaQpSendQueueSize(short size) {
    getBuffer().putShort(RDMA_QP_SEND_QUEUE_SIZE_OFFSET, size);
  }

  @Override
  void initialize() {
    getBuffer().putShort(RECORD_FORMAT_OFFSET, (short) 0);
  }
}
