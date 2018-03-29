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

public class NvmeVersion extends NativeData<NativeBuffer> {

  static final int SIZE = 4;

  private static final int TERITARY_VERSION_NUMBER_OFFSET = 0;
  private static final int MINOR_VERSION_NUMBER_OFFSET = 1;
  private static final int MAJOR_VERSION_NUMBER_OFFSET = 2;

  NvmeVersion(NativeBuffer buffer) {
    super(buffer, SIZE);
  }

  public short getMajor() {
    return getBuffer().getShort(MAJOR_VERSION_NUMBER_OFFSET);
  }

  public byte getMinor() {
    return getBuffer().get(MINOR_VERSION_NUMBER_OFFSET);
  }

  public byte getTertiary() {
    return getBuffer().get(TERITARY_VERSION_NUMBER_OFFSET);
  }

  @Override
  void initialize() {

  }

  @Override
  public String toString() {
    return getMajor() + "." + getMinor() + "." + getTertiary();
  }
}
