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

public class ControllerConfiguration {

  /*
   * NVMf Spec 1.0 - 3.5.1
   */
  private static final int OFFSET = 0x14;
  public static final Property PROPERTY =
      new Property(Property.Size.getInstance().FOUR_BYTES, OFFSET);

  private static final int ENABLE_BITOFFSET = 0;
  private static final int MEMORY_PAGE_SIZE_BITOFFSET_START = 11;
  private static final int MEMORY_PAGE_SIZE_BITOFFSET_END = 13;
  private static final int SHUTDOWN_NOTIFICATION_BITOFFSET_START = 14;
  private static final int SHUTDOWN_NOTIFICATION_BITOFFSET_END = 15;
  private static final int IO_SQE_SIZE_BITOFFSET_START = 16;
  private static final int IO_SQE_SIZE_BITOFFSET_END = 19;
  private static final int IO_CQE_SIZE_BITOFFSET_START = 20;
  private static final int IO_CQE_SIZE_BITOFFSET_END = 23;

  private int value;

  ControllerConfiguration() {
  }

  void update(int value) {
    this.value = value;
  }

  public boolean getEnable() {
    return BitUtil.getBit(value, 0);
  }

  public void setEnable(boolean enable) {
    value = BitUtil.setBitTo(value, ENABLE_BITOFFSET, enable);
  }

  //TODO
  //public IOCommandSetSelected getIOCommandSetSelected() {
  //  return
  //}

  public MemoryPageUnitSize getMemoryPageSize() {
    /* Minimum host memory size is 4KB */
    int mps = BitUtil
        .getBits(value, MEMORY_PAGE_SIZE_BITOFFSET_START, MEMORY_PAGE_SIZE_BITOFFSET_END);
    return new MemoryPageUnitSize(new Pow2Size(mps));
  }

  public static class ShutdownNotification extends EEnum<ShutdownNotification.Value> {

    public class Value extends EEnum.Value {

      Value(int value) {
        super(value);
      }
    }

    // CHECKSTYLE_OFF: MemberNameCheck

    public final Value NO_NOTIFICATION = new Value(0x0);
    public final Value NORMAL_SHUTDOWN = new Value(0x1);
    public final Value ABRUPT_SHUTDOWN = new Value(0x2);

    // CHECKSTYLE_ON: MemberNameCheck

    private ShutdownNotification() {
      super(0x2);
    }

    private static final ShutdownNotification instance = new ShutdownNotification();

    public static ShutdownNotification getInstance() {
      return instance;
    }
  }

  public ShutdownNotification.Value getShutdownNotification() {
    return ShutdownNotification.getInstance()
        .valueOf(BitUtil.getBits(value, SHUTDOWN_NOTIFICATION_BITOFFSET_START,
            SHUTDOWN_NOTIFICATION_BITOFFSET_END));
  }

  public void setShutdownNotification(ShutdownNotification.Value shutdownNotification) {
    value = BitUtil.setBitsTo(value, SHUTDOWN_NOTIFICATION_BITOFFSET_START,
        SHUTDOWN_NOTIFICATION_BITOFFSET_END, shutdownNotification.toInt());
  }

  public QueueEntrySize getIoSubmissionQueueEntrySize() {
    return new QueueEntrySize(
        BitUtil.getBits(value, IO_SQE_SIZE_BITOFFSET_START, IO_SQE_SIZE_BITOFFSET_END));
  }

  public void setIoSubmissionQueueEntrySize(QueueEntrySize size) {
    value = BitUtil
        .setBitsTo(value, IO_SQE_SIZE_BITOFFSET_START, IO_SQE_SIZE_BITOFFSET_END, size.value());
  }

  public QueueEntrySize getIoCompletionQueueEntrySize() {
    return new QueueEntrySize(
        BitUtil.getBits(value, IO_CQE_SIZE_BITOFFSET_START, IO_CQE_SIZE_BITOFFSET_END));
  }

  public void setIoCompletionQueueEntrySize(QueueEntrySize size) {
    value = BitUtil
        .setBitsTo(value, IO_CQE_SIZE_BITOFFSET_START, IO_CQE_SIZE_BITOFFSET_END, size.value());
  }

  int toInt() {
    return value;
  }
}
