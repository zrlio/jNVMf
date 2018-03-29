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

abstract class NativeData<B extends NativeBuffer> {

  private final B buffer;

  private final void setOrder() {
    /* According to NVMf Spec 1.0 - 1.3 conventions */
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
  }

  NativeData(B buffer, int size) {
    if (buffer.remaining() < size) {
      throw new IllegalArgumentException("Buffer size to small");
    }
    buffer.limit(buffer.position() + size);
    this.buffer = (B) buffer.slice();
    setOrder();
  }

  abstract void initialize();

  void reset() {
    getBuffer().clear();
    while (getBuffer().remaining() > Long.BYTES) {
      getBuffer().putLong(0);
    }
    while (getBuffer().remaining() > 0) {
      getBuffer().put((byte) 0);
    }
    initialize();
  }

  /*
   * We do not check if the buffer is null here since
   * we do not want to take the performance hit
   */
  B getBuffer() {
    return buffer;
  }
}
