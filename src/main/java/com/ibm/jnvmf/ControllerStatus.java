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

public class ControllerStatus {

  /*
   * NVMf Spec 1.0 - 3.5.1
   * NVM Spec 1.3a - 3.1.6
   */
  private static final int BASE_OFFSET = 0x1c;
  public static final Property PROPERTY =
      new Property(Property.Size.getInstance().FOUR_BYTES, BASE_OFFSET);

  private static final int READY_BITOFFSET = 0;
  private static final int FATAL_STATUS_BITOFFSET = 1;
  private static final int SHUTDOWN_STATUS_BITOFFSET_START = 2;
  private static final int SHUTDOWN_STATUS_BITOFFSET_END = 3;
  private static final int NVM_SUBSYSTEM_RESET_OCCURED_BITOFFSET = 4;
  private static final int PROCESSING_PAUSED_BITOFFSET = 5;

  private int value;

  ControllerStatus() {
  }

  void update(int value) {
    this.value = value;
  }

  public boolean isReady() {
    return BitUtil.getBit(value, READY_BITOFFSET);
  }

  public boolean isFatalStatus() {
    return BitUtil.getBit(value, FATAL_STATUS_BITOFFSET);
  }

  public static class ShutdownStatus extends EEnum<ShutdownStatus.Value> {

    public class Value extends EEnum.Value {

      Value(int value) {
        super(value);
      }
    }

    // CHECKSTYLE_OFF: MemberNameCheck

    public final Value NORMAL_OPERATION = new Value(0x0);
    public final Value SHUTDOWN_PROCESSING = new Value(0x1);
    public final Value SHUTDOWN_COMPLETE = new Value(0x2);

    // CHECKSTYLE_ON: MemberNameCheck

    private ShutdownStatus() {
      super(0x2);
    }

    private static final ShutdownStatus instance = new ShutdownStatus();

    public static ShutdownStatus getInstance() {
      return instance;
    }
  }

  public ShutdownStatus.Value getShutdownStatus() {
    return ShutdownStatus.getInstance().valueOf(BitUtil.getBits(value,
        SHUTDOWN_STATUS_BITOFFSET_START, SHUTDOWN_STATUS_BITOFFSET_END));
  }

  public boolean hasNvmSubsystemResetOccured() {
    /* only valid if controller supports subsystem reset feature */
    return BitUtil.getBit(value, NVM_SUBSYSTEM_RESET_OCCURED_BITOFFSET);
  }

  public boolean isProcessingPaused() {
    return BitUtil.getBit(value, PROCESSING_PAUSED_BITOFFSET);
  }
}
