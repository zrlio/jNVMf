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

public abstract class NvmIoCommandSqe extends NvmSubmissionQueueEntry {
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

  private final KeyedSglDataBlockDescriptor keyedSglDataBlockDescriptor;
  private final DatasetManagement datasetManagement;

  NvmIoCommandSqe(NativeBuffer buffer) {
    super(buffer);
    this.keyedSglDataBlockDescriptor = new KeyedSglDataBlockDescriptor(getSglDescriptor1Buffer());
    getBuffer().position(DATASET_MANAGEMENT_OFFSET);
    getBuffer().limit(DATASET_MANAGEMENT_OFFSET + DatasetManagement.SIZE);
    this.datasetManagement = new DatasetManagement(getBuffer().slice());
    getBuffer().clear();
  }

  //TODO metadata

  public KeyedSglDataBlockDescriptor getKeyedSglDataBlockDescriptor() {
    return keyedSglDataBlockDescriptor;
  }

  public void setStartingLba(long lba) {
    getBuffer().putLong(STARTING_LBA_OFFSET, lba);
  }

  public void setLimitedRetry(boolean limitedRetry) {
    int raw = getBuffer().get(LIMITED_RETRY_OFFSET);
    raw = BitUtil.setBitTo(raw, LIMITED_RETRY_BITOFFSET, limitedRetry);
    getBuffer().put(LIMITED_RETRY_OFFSET, (byte) raw);
  }

  public void setForceUnitAccess(boolean forceUnitAccess) {
    int raw = getBuffer().get(FORCE_UNIT_ACCESS_OFFSET);
    raw = BitUtil.setBitTo(raw, FORCE_UNIT_ACCESS_BITOFFSET, forceUnitAccess);
    getBuffer().put(FORCE_UNIT_ACCESS_OFFSET, (byte) raw);
  }

  /*public void setProtectionInformationField() {
    TODO: also ILBRT, LBATM, LBAT (+expected for read)
  }*/

  public void setNumberOfLogicalBlocks(int numberOfLogicalBlocks) {
    if (numberOfLogicalBlocks <= 0) {
      throw new IllegalArgumentException("number of logical blocks <= 0");
    }
    if ((numberOfLogicalBlocks & ~((1 << Short.SIZE) - 1)) != 0) {
      throw new IllegalArgumentException("number of logical blocks too large (2 bytes max)");
    }
    getBuffer().putShort(NUMBER_OF_LOGICAL_BLOCKS_OFFSET, (short)(numberOfLogicalBlocks - 1));
  }

  public DatasetManagement getDatasetManagement() {
    return datasetManagement;
  }
}
