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

import com.ibm.disni.verbs.IbvMr;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RdmaByteBuffer extends NativeByteBuffer implements KeyedNativeBuffer {

  private IbvMr mr;

  RdmaByteBuffer(ByteBuffer buffer, IbvMr mr) {
    super(buffer);
    this.mr = mr;
  }

  private void checkValid() {
    if (!isValid()) {
      throw new IllegalStateException("Invalid state - deregistered");
    }
  }

  @Override
  protected RdmaByteBuffer construct(ByteBuffer buffer) {
    return new RdmaByteBuffer(buffer, mr);
  }

  @Override
  public int getRemoteKey() {
    checkValid();
    return mr.getRkey();
  }

  @Override
  public int getLocalKey() {
    checkValid();
    return mr.getLkey();
  }

  @Override
  public void free() throws IOException {
    checkValid();
    mr.deregMr().execute().free();
  }

  @Override
  public boolean isValid() {
    return mr.isOpen();
  }
}
