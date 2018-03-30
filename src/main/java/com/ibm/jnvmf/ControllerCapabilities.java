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
import java.util.concurrent.TimeUnit;

public class ControllerCapabilities {

  /*
   * NVMe Spec 1.3a - 3.1.1
   */
  private static final int OFFSET = 0x00;
  public static final Property PROPERTY =
      new Property(Property.Size.getInstance().EIGHT_BYTES, OFFSET);

  private static final int MAXIMUM_QUEUE_ENTRIES_SUPPORTED_BITOFFSET_START = 0;
  private static final int MAXIMUM_QUEUE_ENTRIES_SUPPORTED_BITOFFSET_END = 15;
  private static final int CONTIGUOUS_QUEUE_ENTRIES_REQUIRED_BITOFFSET = 16;
  private static final int ARBITRATION_MECHANISM_SUPPORTED_BITOFFSET_START = 17;
  private static final int ARBITRATION_MECHANISM_SUPPORTED_BITOFFSET_END = 18;
  private static final int TIMEOUT_BITOFFSET_START = 24;
  private static final int TIMEOUT_BITOFFSET_END = 31;
  private static final int NVM_SUBSYSTEM_RESET_SUPPORTED_BITOFFSET = 36;
  private static final int BOOT_PARTITION_SUPPORT_BITOFFSET = 45;
  private static final int MEMORY_PAGE_SIZE_MINIMUM_BITOFFSET_START = 48;
  private static final int MEMORY_PAGE_SIZE_MINIMUM_BITOFFSET_END = 51;
  private static final int MEMORY_PAGE_SIZE_MAXIMUM_BITOFFSET_START = 52;
  private static final int MEMORY_PAGE_SIZE_MAXIMUM_BITOFFSET_END = 55;

  private long value;

  ControllerCapabilities() {
  }

  void update(long value) {
    this.value = value;
  }

  public long getMaximumQueueEntriesSupported() {
    return BitUtil.getBits(value,
        MAXIMUM_QUEUE_ENTRIES_SUPPORTED_BITOFFSET_START,
        MAXIMUM_QUEUE_ENTRIES_SUPPORTED_BITOFFSET_END) + 1;
  }

  public boolean getContiguousQueuesRequired() {
    /*
     * NVMf Spec 1.0 - 3.5.1
     * CAP.CQR shall be set to fixed value 1h
     */
    return BitUtil.getBit(value, CONTIGUOUS_QUEUE_ENTRIES_REQUIRED_BITOFFSET);
  }

  public static class ArbitrationMechanism extends EEnum<ArbitrationMechanism.Value> {

    public class Value extends EEnum.Value {

      private final String description;

      Value(int value, String description) {
        super(value);
        this.description = description;
      }

      public String getDescription() {
        return description;
      }
    }

    // CHECKSTYLE_OFF: MemberNameCheck

    public final Value WEIGHTED_ROUND_ROBIN = new Value(0x1,
        "Weighted Round Robin with Urgent Priority Class");
    public final Value VENDOR_SPECIFIC = new Value(0x2, "Vendor Specific");

    // CHECKSTYLE_ON: MemberNameCheck

    private ArbitrationMechanism() {
      super(2);
    }

    private static final ArbitrationMechanism instance = new ArbitrationMechanism();

    public static ArbitrationMechanism getInstance() {
      return instance;
    }
  }

  public List<ArbitrationMechanism.Value> getArbitrationMechanismSupported() {
    long ams = (int) BitUtil.getBits(value, ARBITRATION_MECHANISM_SUPPORTED_BITOFFSET_START,
        ARBITRATION_MECHANISM_SUPPORTED_BITOFFSET_END);
    List<ArbitrationMechanism.Value> arbitrationMechanismList = new ArrayList<>(2);
    if ((ams & ArbitrationMechanism.getInstance().WEIGHTED_ROUND_ROBIN.toInt()) != 0) {
      arbitrationMechanismList.add(ArbitrationMechanism.getInstance().WEIGHTED_ROUND_ROBIN);
    }
    if ((ams & ArbitrationMechanism.getInstance().VENDOR_SPECIFIC.toInt()) != 0) {
      arbitrationMechanismList.add(ArbitrationMechanism.getInstance().VENDOR_SPECIFIC);
    }
    return arbitrationMechanismList;
  }

  public long getTimeout() {
    /* in 500 millisecond units */
    return BitUtil.getBits(value, TIMEOUT_BITOFFSET_START, TIMEOUT_BITOFFSET_END) * 500;
  }

  public static TimeUnit getTimeoutUnit() {
    return TimeUnit.MILLISECONDS;
  }

  public boolean getNvmSubsystemResetSupported() {
    return BitUtil.getBit(value, NVM_SUBSYSTEM_RESET_SUPPORTED_BITOFFSET);
  }

  /*public List<CommandSet> getCommandSetSupported() {
    TODO
  }*/

  public boolean getBootPartitionSupport() {
    return BitUtil.getBit(value, BOOT_PARTITION_SUPPORT_BITOFFSET);
  }

  public MemoryPageUnitSize getMemoryPageSizeMinimum() {
    /* 2^(12 = MPSMIN) */
    int mpsMin = (int) BitUtil.getBits(value, MEMORY_PAGE_SIZE_MINIMUM_BITOFFSET_START,
        MEMORY_PAGE_SIZE_MINIMUM_BITOFFSET_END);
    return new MemoryPageUnitSize(new Pow2Size(mpsMin));
  }

  public MemoryPageUnitSize getMemoryPageSizeMaximum() {
    /* 2^(12 = MPSMAX) */
    int mpsMax = (int) BitUtil.getBits(value, MEMORY_PAGE_SIZE_MAXIMUM_BITOFFSET_START,
        MEMORY_PAGE_SIZE_MAXIMUM_BITOFFSET_END);
    return new MemoryPageUnitSize(new Pow2Size(mpsMax));
  }
}
