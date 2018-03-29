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

import java.util.ArrayList;
import java.util.List;

public class IdentifyNamespaceData extends NativeData<KeyedNativeBuffer> {

  public static final int SIZE = 4096;

  /*
   * NVM Spec 1.3a - Figure 114
   */

  private static final int NAMESPACE_SIZE_OFFSET = 0;
  private static final int NAMESPACE_CAPACITY_OFFSET = 8;
  private static final int NAMESPACE_UTILIZATION_OFFSET = 16;
  private static final int NAMESPACE_FEATURES_OFFSET = 24;
  private static final int NUMBER_OF_LBA_FORMATS_OFFSET = 25;
  private static final int FORMATTED_LBA_SIZE_OFFSET = 26;
  private static final int FORMATTED_LBA_SIZE_BITOFFSET_START = 0;
  private static final int FORMATTED_LBA_SIZE_BITOFFSET_END = 3;

  private static final int LBA_FORMAT_0_OFFSET = 128;

  private static final int MAX_LBA_FORMATS = 16;

  private final List<LbaFormat> lbaFormats;

  IdentifyNamespaceData(KeyedNativeBuffer buffer) {
    super(buffer, SIZE);
    this.lbaFormats = new ArrayList<>(MAX_LBA_FORMATS);
  }

  public long getNamespaceSize() {
    return getBuffer().getLong(NAMESPACE_SIZE_OFFSET);
  }

  public long getNamespaceCapacity() {
    return getBuffer().getLong(NAMESPACE_CAPACITY_OFFSET);
  }

  public long getNamespaceUtilization() {
    return getBuffer().getLong(NAMESPACE_UTILIZATION_OFFSET);
  }

  private byte getNumberOfLbaFormats() {
    int raw = getBuffer().get(NUMBER_OF_LBA_FORMATS_OFFSET);
    if (raw > MAX_LBA_FORMATS || raw < 0) {
      throw new IllegalStateException("invalid number of LBA formats");
    }
    // zero based
    return (byte) (raw + 1);
  }

  private void updateLbaFormats() {
    lbaFormats.clear();
    int numberOfLbaFormats = getNumberOfLbaFormats();
    for (int i = 0; i < numberOfLbaFormats; i++) {
      int newPosition = LBA_FORMAT_0_OFFSET + LbaFormat.SIZE * i;
      getBuffer().limit(newPosition + LbaFormat.SIZE);
      getBuffer().position(newPosition);
      lbaFormats.add(LbaFormat.fromBuffer(getBuffer().slice()));
    }
    getBuffer().clear();
  }

  public List<LbaFormat> getSupportedLbaFormats() {
    updateLbaFormats();
    return lbaFormats;
  }

  private int getFormattedLbaSizeIndex() {
    int raw = getBuffer().get(FORMATTED_LBA_SIZE_OFFSET);
    return BitUtil.getBits(raw, FORMATTED_LBA_SIZE_BITOFFSET_START,
        FORMATTED_LBA_SIZE_BITOFFSET_END);
  }

  public LbaFormat getFormattedLbaSize() {
    updateLbaFormats();
    return lbaFormats.get(getFormattedLbaSizeIndex());
  }

  @Override
  void initialize() {

  }
}
