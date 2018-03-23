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

public abstract class NvmIOCommandSQE extends NvmSubmissionQueueEntry {
    /*
     * NVM Spec 1.3a - 6
     */

    private static final int STARTING_LBA_OFFSET = 40;
    private static final int NUMBER_OF_LOGICAL_BLOCKS_OFFSET = 48;
    private static final int FORCE_UNIT_ACCESS_OFFSET = 51;
    private static final int FORCE_UNIT_ACCESS_BITOFFSET = 6;
    private static final int LIMITED_RETRY_OFFSET = 51;
    private static final int LIMITED_RETRY_BITOFFSET = 7;
    private static final int DATASET_MANAGEMENT_OFFSET = 52;

    private final KeyedSGLDataBlockDescriptor keyedSGLDataBlockDescriptor;
    private final DatasetManagement datasetManagement;

    NvmIOCommandSQE(NativeBuffer buffer) {
        super(buffer);
        this.keyedSGLDataBlockDescriptor = new KeyedSGLDataBlockDescriptor(getSGLDescriptor1Buffer());
        getBuffer().position(DATASET_MANAGEMENT_OFFSET);
        getBuffer().limit(DATASET_MANAGEMENT_OFFSET + DatasetManagement.SIZE);
        this.datasetManagement = new DatasetManagement(getBuffer().slice());
        getBuffer().clear();
    }

    //TODO metadata

    KeyedSGLDataBlockDescriptor getKeyedSGLDataBlockDescriptor() {
        return keyedSGLDataBlockDescriptor;
    }

    public void setStartingLBA(long lba) {
        getBuffer().putLong(STARTING_LBA_OFFSET, lba);
    }

    public void setLimitedRetry(boolean limitedRetry) {
        int b = getBuffer().get(LIMITED_RETRY_OFFSET);
        b = BitUtil.setBitTo(b, LIMITED_RETRY_BITOFFSET, limitedRetry);
        getBuffer().put(LIMITED_RETRY_OFFSET, (byte) b);
    }

    public void setForceUnitAccess(boolean forceUnitAccess) {
        int b = getBuffer().get(FORCE_UNIT_ACCESS_OFFSET);
        b = BitUtil.setBitTo(b, FORCE_UNIT_ACCESS_BITOFFSET, forceUnitAccess);
        getBuffer().put(FORCE_UNIT_ACCESS_OFFSET, (byte) b);
    }

//	public void setProtectionInformationField() {
//  TODO: also ILBRT, LBATM, LBAT (+expected for read)
//	}

    public void setNumberOfLogicalBlocks(short numberOfLogicalBlocks) {
        /* 0-based */
        getBuffer().putShort(NUMBER_OF_LOGICAL_BLOCKS_OFFSET, numberOfLogicalBlocks);
    }

    public DatasetManagement getDatasetManagement() {
        return datasetManagement;
    }
}
