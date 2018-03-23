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


public class SGLDataBlockDescriptor extends ScatterGatherListDescriptor {
    private final static int ADDRESS_OFFSET = 0;
    private final static int LENGTH_OFFSET = 8;

    static class SubType extends ScatterGatherListDescriptor.SubType {
        class Value extends ScatterGatherListDescriptor.SubType.Value {
            Value(int value) {
                super(value);
            }
        }

        /* address field specifies starting 64bit address of data block */
        public final Value ADDRESS = new Value(0x0);
        public final Value OFFSET = new Value(0x1);

        private SubType() {
        }

        private final static SubType instance = new SubType();

        public static SubType getInstance() {
            return instance;
        }
    }

    SGLDataBlockDescriptor(NativeBuffer buffer) {
        super(buffer);
    }

    private void setSubType(SubType.Value subType) {
        setIdentifier(Type.getInstance().SGL_DATABLOCK, subType);
    }

    void setAddress(long address) {
        setSubType(SubType.getInstance().ADDRESS);
        getBuffer().putLong(ADDRESS_OFFSET, address);
    }

    void setOffset(long offset) {
        setSubType(SubType.getInstance().OFFSET);
        getBuffer().putLong(ADDRESS_OFFSET, offset);
    }

    void setLength(int length) {
        getBuffer().putInt(LENGTH_OFFSET, length);
    }

    @Override
    void initialize() {

    }
}
