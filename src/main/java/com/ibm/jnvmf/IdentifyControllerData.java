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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class IdentifyControllerData extends NativeData<KeyedNativeBuffer> {
  /*
   * NVMe Spec 1.3a - 3.1.5
   */

  public static final int SIZE = 4096;

  private static final int PCI_VENDOR_ID_OFFSET = 0;
  private static final int PCI_SUBSYSTEM_VENDOR_ID_OFFSET = 2;
  private static final int SERIAL_NUMBER_OFFSET = 4;
  private static final int SERIAL_NUMBER_LENGTH = 20;
  private static final int MODEL_NUMBER_OFFSET = 24;
  private static final int MODEL_NUMBER_LENGTH = 40;
  private static final int FIRMWARE_REVISION_OFFSET = 64;
  private static final int FIRMWARE_REVISION_LENGTH = 8;

  private static final int MAXIMUM_DATA_TRANSFER_SIZE_OFFSET = 77;
  private static final int CONTROLLER_ID_OFFSET = 78;
  /* Only since NVMe 1.2 otherwise zero */
  private static final int VERSION_OFFSET = 80;

  private static final int LOG_PAGE_ATTRIBUTES_OFFSET = 261;
  private static final int ERROR_LOG_PAGE_ENTRIES = 262;

  private static final int SUBMISSION_QUEUE_ENTRY_SIZE = 512;
  private static final int REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_START = 0;
  private static final int REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_END = 3;
  private static final int MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_START = 4;
  private static final int MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_END = 7;
  private static final int COMPLETION_QUEUE_ENTRY_SIZE = 513;
  private static final int REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_START = 0;
  private static final int REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_END = 3;
  private static final int MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_START = 4;
  private static final int MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_END = 7;

  /* NVMf enhancements */
  private static final int IO_QUEUE_COMMAND_CAPSULE_SUPPORTED_SIZE_OFFSET = 1792;
  private static final int IO_QUEUE_RESPONSE_CAPSULE_SUPPORTED_SIZE_OFFSET = 1796;
  private static final int IN_CAPSULE_DATA_OFFSET_OFFSET = 1800;
  private static final int CONTROLLER_ATTRIBUTTES_OFFSET = 1802;
  private static final int MAXIMUM_SGL_DATA_BLOCK_DESCRIPTORS_OFFSET = 1803;

  private final ByteBuffer serialNumberBuffer;
  private final ByteBuffer modelNumberBuffer;
  private final ByteBuffer firmwareRevisionBuffer;
  private final NvmeVersion nvmeVersion;

  private final ControllerCapabilities controllerCapabilities;

  private ByteBuffer sliceBuffer(int offset, int length) {
    getBuffer().position(offset);
    getBuffer().limit(getBuffer().position() + length);
    ByteBuffer buffer = getBuffer().sliceToByteBuffer();
    getBuffer().clear();
    return buffer;
  }

  private String decode(ByteBuffer buffer) {
    return StandardCharsets.US_ASCII.decode(buffer).toString().trim();
  }

  IdentifyControllerData(KeyedNativeBuffer buffer,
      ControllerCapabilities controllerCapabilities) {
    super(buffer, SIZE);
    this.serialNumberBuffer = sliceBuffer(SERIAL_NUMBER_OFFSET, SERIAL_NUMBER_LENGTH);
    this.modelNumberBuffer = sliceBuffer(MODEL_NUMBER_OFFSET, MODEL_NUMBER_LENGTH);
    this.firmwareRevisionBuffer = sliceBuffer(FIRMWARE_REVISION_OFFSET, FIRMWARE_REVISION_LENGTH);
    getBuffer().position(VERSION_OFFSET);
    getBuffer().limit(getBuffer().position() + NvmeVersion.SIZE);
    this.nvmeVersion = new NvmeVersion(getBuffer().slice());
    getBuffer().clear();
    this.controllerCapabilities = controllerCapabilities;
  }

  public short getPciVendorId() {
    return getBuffer().getShort(PCI_VENDOR_ID_OFFSET);
  }

  public short getPciSubsystemVendorId() {
    return getBuffer().getShort(PCI_SUBSYSTEM_VENDOR_ID_OFFSET);
  }

  public String getSerialNumber() {
    serialNumberBuffer.clear();
    return decode(serialNumberBuffer);
  }

  public String getModelNumber() {
    modelNumberBuffer.clear();
    return decode(modelNumberBuffer);
  }

  public String getFirmwareRevision() {
    firmwareRevisionBuffer.clear();
    return decode(firmwareRevisionBuffer);
  }

  public MemoryPageUnitSize getMaximumDataTransferSize() {
    /* unit of minimum memory page size (CAP.MPSMIN) as power of two */
    int mdts = getBuffer().get(MAXIMUM_DATA_TRANSFER_SIZE_OFFSET);
    if (mdts == 0) {
      /* A value of 0h indicates no restrictions on transfer size. */
      return MemoryPageUnitSize.MAX_VALUE;
    } else {
      return new MemoryPageUnitSize(controllerCapabilities.getMemoryPageSizeMinimum().toPow2Size(),
          new Pow2Size(mdts));
    }
  }

  public ControllerId getControllerId() {
    return ControllerId.valueOf(getBuffer().getShort(CONTROLLER_ID_OFFSET));
  }

  public NvmeVersion getNvmeVersion() {
    return nvmeVersion;
  }

  public QueueEntrySize getRequiredSubmissionQueueEntrySize() {
    int raw = getBuffer().get(SUBMISSION_QUEUE_ENTRY_SIZE);
    return new QueueEntrySize(
        BitUtil.getBits(raw, REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_START,
            REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_END));
  }

  public QueueEntrySize getMaximumSubmissionQueueEntrySize() {
    int raw = getBuffer().get(SUBMISSION_QUEUE_ENTRY_SIZE);
    return new QueueEntrySize(
        BitUtil.getBits(raw, MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_START,
            MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_END));
  }

  public QueueEntrySize getRequiredCompletionQueueEntrySize() {
    int raw = getBuffer().get(COMPLETION_QUEUE_ENTRY_SIZE);
    return new QueueEntrySize(BitUtil.getBits(raw,
        REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_START, REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_END));
  }

  public QueueEntrySize getMaximumCompletionQueueEntrySize() {
    int raw = getBuffer().get(COMPLETION_QUEUE_ENTRY_SIZE);
    return new QueueEntrySize(
        BitUtil.getBits(raw, MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_START,
            MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_END));
  }

  public static class LogPageAttributes {
    private static final int SMART_HEALTH_INFORMATION_SUPPORT_BITOFFSET = 0;
    private static final int COMMANDS_SUPPORTED_AND_EFFECTS_LOG_PAGE_SUPPORT_BITOFFSET = 1;
    private static final int GET_LOG_PAGE_EXTENDED_DATA_SUPPORT_BITOFFSET = 2;
    private static final int TELEMETRY_HOST_CONTROLLER_INITIATED_SUPPORT_BITOFFSET = 3;

    private final boolean smartHealthInformationSupport;
    private final boolean commandsSupportedAndEffectsLogPageSupport;
    private final boolean getLogPageExtendedDataSupport;
    private final boolean telemetryHostControllerInitiatedSupport;

    private LogPageAttributes(int raw) {
      this.smartHealthInformationSupport =
          BitUtil.getBit(raw, SMART_HEALTH_INFORMATION_SUPPORT_BITOFFSET);
      this.commandsSupportedAndEffectsLogPageSupport =
          BitUtil.getBit(raw, COMMANDS_SUPPORTED_AND_EFFECTS_LOG_PAGE_SUPPORT_BITOFFSET);
      this.getLogPageExtendedDataSupport =
          BitUtil.getBit(raw, GET_LOG_PAGE_EXTENDED_DATA_SUPPORT_BITOFFSET);
      this.telemetryHostControllerInitiatedSupport =
          BitUtil.getBit(raw, TELEMETRY_HOST_CONTROLLER_INITIATED_SUPPORT_BITOFFSET);
    }

    public boolean getSmartHealthInformationSupport() {
      return smartHealthInformationSupport;
    }

    public boolean getCommandsSupportedAndEffectsLogPageSupport() {
      return commandsSupportedAndEffectsLogPageSupport;
    }

    public boolean hasGetLogPageExtendedDataSupport() {
      return getLogPageExtendedDataSupport;
    }

    public boolean getTelemetryHostControllerInitiatedSupport() {
      return telemetryHostControllerInitiatedSupport;
    }
  }

  public LogPageAttributes getLogPageAttributes() {
    return new LogPageAttributes(getBuffer().get(LOG_PAGE_ATTRIBUTES_OFFSET));
  }

  public int getErrorLogPageEntries() {
    /* 0-based */
    return getBuffer().get(ERROR_LOG_PAGE_ENTRIES) + 1;
  }

  public long getIoQueueCommandCapsuleSupportedSize() {
    /*
     * NVMf Spec 1.0 - 4.1
     * ...in 16 byte units
     */
    return getBuffer().getInt(IO_QUEUE_COMMAND_CAPSULE_SUPPORTED_SIZE_OFFSET) * 16L;
  }

  public long getIoQueueResponseCapsuleSupportedSize() {
    /*
     * NVMf Spec 1.0 - 4.1
     * ...in 16 byte units
     */
    return getBuffer().getInt(IO_QUEUE_RESPONSE_CAPSULE_SUPPORTED_SIZE_OFFSET) * 16L;
  }

  public int getInCapsuleDataOffset() {
    /*
     * NVMf Spec 1.0 - 4.1
     * ...in 16 byte units
     */
    return getBuffer().getShort(IN_CAPSULE_DATA_OFFSET_OFFSET) * 16;
  }

  public boolean isStaticController() {
    return BitUtil.getBit(getBuffer().get(CONTROLLER_ATTRIBUTTES_OFFSET), 0);
  }

  public byte getMaximumSglDataBlockDescriptors() {
    return getBuffer().get(MAXIMUM_SGL_DATA_BLOCK_DESCRIPTORS_OFFSET);
  }

  @Override
  void initialize() {

  }

}
