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

    public final static int SIZE = 4096;

    private final static int PCI_VENDOR_ID_OFFSET = 0;
    private final static int PCI_SUBSYSTEM_VENDOR_ID_OFFSET = 2;
    private final static int SERIAL_NUMBER_OFFSET = 4;
    private final static int SERIAL_NUMBER_LENGTH = 20;
    private final static int MODEL_NUMBER_OFFSET = 24;
    private final static int MODEL_NUMBER_LENGTH = 40;
    private final static int FIRMWARE_REVISION_OFFSET = 64;
    private final static int FIRMWARE_REVISION_LENGTH = 8;

    private final static int MAXIMUM_DATA_TRANSFER_SIZE_OFFSET = 77;
    private final static int CONTROLLER_ID_OFFSET = 78;
    /* Only since NVMe 1.2 otherwise zero */
    private final static int VERSION_OFFSET = 80;

    private final static int SUBMISSION_QUEUE_ENTRY_SIZE = 512;
    private final static int REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_START = 0;
    private final static int REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_END = 3;
    private final static int MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_START = 4;
    private final static int MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_END = 7;
    private final static int COMPLETION_QUEUE_ENTRY_SIZE = 513;
    private final static int REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_START = 0;
    private final static int REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_END = 3;
    private final static int MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_START = 4;
    private final static int MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_END = 7;

    /* NVMf enhancements */
    private final static int IO_QUEUE_COMMAND_CAPSULE_SUPPORTED_SIZE_OFFSET = 1792;
    private final static int IO_QUEUE_RESPONSE_CAPSULE_SUPPORTED_SIZE_OFFSET = 1796;
    private final static int IN_CAPSULE_DATA_OFFSET_OFFSET = 1800;
    private final static int CONTROLLER_ATTRIBUTTES_OFFSET = 1802;
    private final static int MAXIMUM_SGL_DATA_BLOCK_DESCRIPTORS_OFFSET = 1803;

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

    public IdentifyControllerData(KeyedNativeBuffer buffer, ControllerCapabilities controllerCapabilities) {
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

    public short getPCIVendorId() {
        return getBuffer().getShort(PCI_VENDOR_ID_OFFSET);
    }

    public short getPCISubsystemVendorId() {
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
        return new MemoryPageUnitSize(controllerCapabilities.getMemoryPageSizeMinimum().toPow2Size(), new Pow2Size(mdts));
    }

    public ControllerID getControllerId() {
        return ControllerID.valueOf(getBuffer().getShort(CONTROLLER_ID_OFFSET));
    }

    public NvmeVersion getNvmeVersion() {
        return nvmeVersion;
    }

    public QueueEntrySize getRequiredSubmissionQueueEntrySize() {
        int b = getBuffer().get(SUBMISSION_QUEUE_ENTRY_SIZE);
        return new QueueEntrySize(
                BitUtil.getBits(b, REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_START, REQUIRED_SQE_ENTRY_SIZE_BITOFFSET_END));
    }

    public QueueEntrySize getMaximumSubmissionQueueEntrySize() {
        int b = getBuffer().get(SUBMISSION_QUEUE_ENTRY_SIZE);
        return new QueueEntrySize(
                BitUtil.getBits(b, MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_START, MAXIMUM_SQE_ENTRY_SIZE_BITOFFSET_END));
    }

    public QueueEntrySize getRequiredCompletionQueueEntrySize() {
        int b = getBuffer().get(COMPLETION_QUEUE_ENTRY_SIZE);
        return new QueueEntrySize(BitUtil.getBits(b,
                REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_START, REQUIRED_CQE_ENTRY_SIZE_BITOFFSET_END));
    }

    public QueueEntrySize getMaximumCompletionQueueEntrySize() {
        int b = getBuffer().get(COMPLETION_QUEUE_ENTRY_SIZE);
        return new QueueEntrySize(
                BitUtil.getBits(b, MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_START, MAXIMUM_CQE_ENTRY_SIZE_BITOFFSET_END));
    }

    public long getIOQueueCommandCapsuleSupportedSize() {
        /*
         * NVMf Spec 1.0 - 4.1
         * ...in 16 byte units
         */
        return getBuffer().getInt(IO_QUEUE_COMMAND_CAPSULE_SUPPORTED_SIZE_OFFSET) * 16L;
    }

    public long getIOQueueResponseCapsuleSupportedSize() {
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

    public byte getMaximumSGLDataBlockDescriptors() {
        return getBuffer().get(MAXIMUM_SGL_DATA_BLOCK_DESCRIPTORS_OFFSET);
    }

    @Override
    void initialize() {

    }
}
