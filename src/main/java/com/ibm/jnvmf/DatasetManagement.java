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

public class DatasetManagement extends NativeData<NativeBuffer> {
    /*
     * NVM Spec 1.3a - 6.9/6.14
     */


    static final int SIZE = 1;

    private final static int ACCESS_FREQUENCY_BITOFFSET_START = 0;
    private final static int ACCESS_FREQUENCY_BITOFFSET_END = 3;
    private final static int ACCESS_LATENCY_BITOFFSET_START = 4;
    private final static int ACCESS_LATENCY_BITOFFSET_END = 5;
    private final static int SEQUENTIAL_REQUEST_BITOFFSET = 6;
    private final static int INCOMPRESSIBLE_BITOFFSET = 7;

    DatasetManagement(NativeBuffer buffer) {
        super(buffer, 1);
    }

    public void setIncompressible(boolean incompressible) {
        int b = getBuffer().get(0);
        b = BitUtil.setBitTo(b, INCOMPRESSIBLE_BITOFFSET, incompressible);
        getBuffer().put(0, (byte) b);
    }

    public void setSequentialRequest(boolean sequentialRequest) {
        int b = getBuffer().get(0);
        b = BitUtil.setBitTo(b, SEQUENTIAL_REQUEST_BITOFFSET, sequentialRequest);
        getBuffer().put(0, (byte) b);
    }

    public static class AccessLatency extends EEnum<AccessLatency.Value> {
        public class Value extends EEnum.Value {
            Value(int value) {
                super(value);
            }
        }

        public final Value NONE = new Value(0x0);
        public final Value IDLE = new Value(0x1);
        public final Value NORMAL = new Value(0x2);
        public final Value LOW = new Value(0x3);

        private AccessLatency() {
            super(0x3);
        }

        private final static AccessLatency instance = new AccessLatency();

        public static AccessLatency getInstance() {
            return instance;
        }
    }

    public void setAccessLatency(AccessLatency.Value accessLatency) {
        int b = getBuffer().get(0);
        b = BitUtil.setBitsTo(b, ACCESS_LATENCY_BITOFFSET_START, ACCESS_LATENCY_BITOFFSET_END,
                accessLatency.toInt());
        getBuffer().put(0, (byte) b);
    }

    public static class AccessFrequency extends EEnum<AccessFrequency.Value> {
        public class Value extends EEnum.Value {
            Value(int value) {
                super(value);
            }
        }

        public final Value NO_INFORMATION = new Value(0x0);
        public final Value TYPICAL = new Value(0x1);
        public final Value INFREQUENT = new Value(0x2);
        public final Value INFREQUENT_WRITES_FREQUENT_READS = new Value(0x3);
        public final Value FREQUENT_WRITES_INFREQUENT_READS = new Value(0x4);
        public final Value FREQUENT = new Value(0x5);
        public final Value ONE_TIME_READ = new Value(0x6);
        public final Value SPECULATIVE_READ = new Value(0x7);
        public final Value OVERWRITTEN_NEAR_FUTURE = new Value(0x8);

        private AccessFrequency() {
            super(0x8);
        }

        private final static AccessFrequency instance = new AccessFrequency();

        public static AccessFrequency getInstance() {
            return instance;
        }
    }

    public void setAccessFrequency(AccessFrequency.Value accessFrequency) {
        int b = getBuffer().get(0);
        b = BitUtil.setBitsTo(b, ACCESS_FREQUENCY_BITOFFSET_START, ACCESS_FREQUENCY_BITOFFSET_END,
                accessFrequency.toInt());
        getBuffer().put(0, (byte) b);
    }

    @Override
    void initialize() {

    }
}
