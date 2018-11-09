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

import com.ibm.disni.verbs.SVCPostRecv;
import com.ibm.disni.verbs.StatefulVerbCall;

import java.io.IOException;

class RdmaRecv implements StatefulVerbCall<RdmaRecv> {

  private final SVCPostRecv postRecv;
  private final KeyedNativeBuffer buffer;

  RdmaRecv(SVCPostRecv postRecv, KeyedNativeBuffer buffer) {
    this.postRecv = postRecv;
    this.buffer = buffer;
  }

  @Override
  public RdmaRecv execute() throws IOException {
    postRecv.execute();
    return this;
  }

  @Override
  public boolean isValid() {
    return postRecv.isValid();
  }

  @Override
  public RdmaRecv free() {
    postRecv.free();
    return this;
  }

  public KeyedNativeBuffer getBuffer() {
    return buffer;
  }
}
