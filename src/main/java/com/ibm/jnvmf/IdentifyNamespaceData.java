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

    private final static int NAMESPACE_SIZE_OFFSET = 0;
    private final static int NAMESPACE_CAPACITY_OFFSET = 8;
    private final static int NAMESPACE_UTILIZATION_OFFSET = 16;
    private final static int NAMESPACE_FEATURES_OFFSET = 24;
    private final static int NUMBER_OF_LBA_FORMATS_OFFSET = 25;
    private final static int FORMATTED_LBA_SIZE_OFFSET = 26;
    private final static int FORMATTED_LBA_SIZE_BITOFFSET_START = 0;
    private final static int FORMATTED_LBA_SIZE_BITOFFSET_END = 3;

    private final static int LBA_FORMAT_0_OFFSET = 128;

    private final static int MAX_LBA_FORMATS = 16;

    private final List<LBAFormat> lbaFormats;

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

    private byte getNumberOfLBAFormats() {
        int b = getBuffer().get(NUMBER_OF_LBA_FORMATS_OFFSET);
        if (b > MAX_LBA_FORMATS || b < 0) {
            throw new IllegalStateException("invalid number of LBA formats");
        }
        // zero based
        return (byte) (b + 1);
    }

    private void updateLBAFormats() {
        lbaFormats.clear();
        int numberOfLBAFormats = getNumberOfLBAFormats();
        for (int i = 0; i < numberOfLBAFormats; i++) {
            int newPosition = LBA_FORMAT_0_OFFSET + LBAFormat.SIZE * i;
            getBuffer().limit(newPosition + LBAFormat.SIZE);
            getBuffer().position(newPosition);
            lbaFormats.add(LBAFormat.fromBuffer(getBuffer().slice()));
        }
        getBuffer().clear();
    }

    public List<LBAFormat> getSupportedLBAFormats() {
        updateLBAFormats();
        return lbaFormats;
    }

    private int getFormattedLbaSizeIndex() {
        int b = getBuffer().get(FORMATTED_LBA_SIZE_OFFSET);
        return BitUtil.getBits(b, FORMATTED_LBA_SIZE_BITOFFSET_START, FORMATTED_LBA_SIZE_BITOFFSET_END);
    }

    public LBAFormat getFormattedLBASize() {
        updateLBAFormats();
        return lbaFormats.get(getFormattedLbaSizeIndex());
    }

    @Override
    void initialize() {

    }
}
