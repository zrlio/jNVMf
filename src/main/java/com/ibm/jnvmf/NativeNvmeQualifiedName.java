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

import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;

public class NativeNvmeQualifiedName extends NativeData<NativeBuffer> {

  public static final int SIZE = 223;

  /*
   * Nvme Spec 1.3a - 7.9
   *
   * UTF-8 encoded string
   */

  NativeNvmeQualifiedName(NativeBuffer buffer) {
    super(buffer, SIZE);
  }

  public void set(NvmeQualifiedName nqn) {
    getBuffer().position(0);
    try {
      getBuffer().put(StandardCharsets.UTF_8.encode(nqn.toString()));
    } catch (BufferOverflowException exception) {
      throw new IllegalArgumentException(
          "NVMe qualified name to large - maximum name is 223 bytes in length");
    }
  }

  NvmeQualifiedName get() {
    getBuffer().clear();
    String nqn = StandardCharsets.UTF_8.decode(getBuffer().sliceToByteBuffer()).toString();
    /* String is NULL terminated => remove unnecessary parts */
    nqn = nqn.substring(0, nqn.indexOf('\0'));
    return new NvmeQualifiedName(nqn);
  }

  @Override
  void initialize() {

  }
}
