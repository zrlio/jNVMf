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

public abstract class AdminSubmissionQeueueEntry extends SubmissionQueueEntry {

  private static int SGL_USE_OFFSET = 1;
  private static int SGL_USE_BITOFFSET = 6;
  private static int NAMESPACE_IDENTIFIER_OFFSET = 4;

  AdminSubmissionQeueueEntry(NativeBuffer buffer) {
    super(buffer);
  }

  private void setSglUse() {
    /*
     * NVMe Spec 1.3a - 4.2
     *
     * 01b for NVMf => SGLs are used for this transfer
     */
    int sndByte = getBuffer().get(SGL_USE_OFFSET);
    sndByte = BitUtil.setBit(sndByte, SGL_USE_BITOFFSET);
    getBuffer().put(SGL_USE_OFFSET, (byte) sndByte);
  }

  void setNamespaceIdentifier(NamespaceIdentifier namespaceIdentifier) {
    getBuffer().putInt(NAMESPACE_IDENTIFIER_OFFSET, namespaceIdentifier.toInt());
  }

  @Override
  void initialize() {
    super.initialize();
    setSglUse();
  }
}
