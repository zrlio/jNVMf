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

import java.io.IOException;

public abstract class CommandCapsule<Sqe extends SubmissionQueueEntry> extends NativeData<KeyedNativeBuffer>
        implements Freeable {

    /*
     * NVMf Spec 1.0 - 2
     *
     * N-1:M Data (if present)
     * M-1:64 Additional SGLs (if present) where M-1 max ICDOFF * 16 + 64
     * 63:0 Sqe
     *
     */

    private final Sqe submissionQueueEntry;
    private final NativeBuffer data;
    private final int incapsuleDataOffset;
    private final ScatterGatherListDescriptor[] additionalSGLs;

    CommandCapsule(KeyedNativeBuffer buffer, SubmissionQueueEntryFactory<Sqe> sqeFactory) {
        this(buffer, sqeFactory, 0, 0, 0);
    }

    static int computeCommandCapsuleSize(int additionalSGLs, int incapsuleDataOffset, int incapsuleDataSize) {
        int maxCommandCapsuleSize = SubmissionQueueEntry.SIZE + additionalSGLs * ScatterGatherListDescriptor.SIZE;
        if (incapsuleDataSize > 0) {
            try {
                maxCommandCapsuleSize = Math.max(
                        Math.addExact(SubmissionQueueEntry.SIZE + incapsuleDataOffset, incapsuleDataSize),
                        maxCommandCapsuleSize);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Incapsule data size too large (integer overflow)");
            }
        }
        return maxCommandCapsuleSize;
    }

    CommandCapsule(KeyedNativeBuffer buffer, SubmissionQueueEntryFactory<Sqe> sqeFactory,
                   int additionalSGLs, int incapsuleDataOffset, int incapsuleDataSize) {
        super(buffer, computeCommandCapsuleSize(additionalSGLs, incapsuleDataOffset, incapsuleDataSize));
        this.submissionQueueEntry = sqeFactory.construct(getBuffer());
        this.submissionQueueEntry.initialize();
        this.incapsuleDataOffset = incapsuleDataOffset;
        if (incapsuleDataSize > 0) {
            getBuffer().position(SubmissionQueueEntry.SIZE + incapsuleDataOffset);
            getBuffer().limit(getBuffer().position() + incapsuleDataSize);
            this.data = getBuffer().slice();
            getBuffer().clear();
        } else {
            this.data = null;
        }

        this.additionalSGLs = new ScatterGatherListDescriptor[additionalSGLs];
    }

    int getIncapsuleDataOffset() {
        return incapsuleDataOffset;
    }

    public Sqe getSubmissionQueueEntry() {
        return submissionQueueEntry;
    }

    NativeBuffer getIncapsuleData() {
        if (data == null) {
            throw new IllegalStateException("Command does not support incasule data or not enough space");
        }
        return data;
    }

    //TODO handle additional SGLs

    @Override
    void initialize() {
        submissionQueueEntry.initialize();
    }

    @Override
    KeyedNativeBuffer getBuffer() {
        return super.getBuffer();
    }

    @Override
    public void free() throws IOException {
        getBuffer().free();
    }

    @Override
    public boolean isValid() {
        return getBuffer().isValid();
    }
}
