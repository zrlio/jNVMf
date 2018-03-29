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

public class RdmaCmAcceptPrivateData extends NativeData<NativeBuffer> {

  public static final int SIZE = 32;
  private static final int RECORD_FORMAT_OFFSET = 0;
  private static final int RDMA_QP_RECEIVE_QUEUE_SIZE_OFFSET = 2;

  //TODO implement in DiSNI
  RdmaCmAcceptPrivateData(NativeBuffer buffer) {
    super(buffer, SIZE);
  }

  short getRecordFormat() {
    return getBuffer().getShort(RECORD_FORMAT_OFFSET);
  }

  short getRdmaQpReceiveQueueSize() {
    return getBuffer().getShort(RDMA_QP_RECEIVE_QUEUE_SIZE_OFFSET);
  }

  @Override
  void initialize() {
  }
}
