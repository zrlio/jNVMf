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

public class FabricsConnectCommandData extends NativeData<KeyedNativeBuffer> {
    public final static int SIZE = 1024;
    private final static int HOST_IDENTIFIER_OFFSET = 0;
    private final static int CONTROLLER_ID_OFFSET = 16;
    private final static int SUBSYSTEM_NVME_QUALIFIED_NAME_OFFSET = 256;
    private final static int HOST_NVME_QUALIFIED_NAME_OFFSET = 512;

    private final NativeNvmeQualifiedName subsystemNvmeQualifiedName;
    private final NativeNvmeQualifiedName hostNvmeQualifiedName;


    FabricsConnectCommandData(KeyedNativeBuffer buffer) {
        super(buffer, SIZE);
        buffer.position(SUBSYSTEM_NVME_QUALIFIED_NAME_OFFSET);
        this.subsystemNvmeQualifiedName = new NativeNvmeQualifiedName(buffer);
        buffer.clear();
        buffer.position(HOST_NVME_QUALIFIED_NAME_OFFSET);
        this.hostNvmeQualifiedName = new NativeNvmeQualifiedName(buffer);
        buffer.clear();
    }

    void setControllerId(ControllerID controllerId) {
        getBuffer().putShort(CONTROLLER_ID_OFFSET, controllerId.toShort());
    }

    void setSubsystemNVMeQualifiedName(NvmeQualifiedName nvmeQualifiedName) {
        this.subsystemNvmeQualifiedName.set(nvmeQualifiedName);
    }

    void setHostNVMeQualifiedName(NvmeQualifiedName nvmeQualifiedName) {
        this.hostNvmeQualifiedName.set(nvmeQualifiedName);
    }

    @Override
    void initialize() {
        getBuffer().position(HOST_IDENTIFIER_OFFSET);
        FabricsHostIdentifier.getInstance().get(getBuffer());
        getBuffer().clear();
    }
}
