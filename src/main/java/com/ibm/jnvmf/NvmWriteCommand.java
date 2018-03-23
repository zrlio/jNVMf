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
import java.util.concurrent.ExecutionException;

public class NvmWriteCommand extends NvmIOCommand<NvmWriteCommandCapsule> {

    private static NvmWriteCommandCapsule newNvmWriteCommandCapsule(IOQueuePair queuePair) throws IOException {
        int inCapsuleDataOffset = queuePair.getController().getIdentifyControllerData().getInCapsuleDataOffset();
        return new NvmWriteCommandCapsule(queuePair.allocateCommandCapsule(),
                queuePair.getMaximumAdditionalSGLs(), inCapsuleDataOffset, queuePair.getInCapsuleDataSize());
    }

    public NvmWriteCommand(IOQueuePair queuePair) throws IOException {
        super(queuePair, newNvmWriteCommandCapsule(queuePair));
    }

    public NativeBuffer getIncapsuleData() throws ExecutionException, InterruptedException {
        return getCommandCapsule().getIncapsuleData();
    }

    public void setIncapsuleData(NativeBuffer incapsuleData) throws ExecutionException, InterruptedException {
        //TODO support to set buffer outside of capsule by using RDMA sgls
        //TODO change RDMA sgl accordingly => we don't want to send unnecessary data on the wire
        getCommandCapsule().setIncapsuleData(incapsuleData);
    }
}
