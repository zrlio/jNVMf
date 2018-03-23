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

public class ControllerID {
    private final short id;

    public final static ControllerID ADMIN_DYNAMIC = new ControllerID((short) 0xFFFF);
    public final static ControllerID ADMIN_STATIC = new ControllerID((short) 0xFFFE);


    public ControllerID(short id) {
        this.id = id;
    }

    public short toShort() {
        return id;
    }

    public static ControllerID valueOf(short id) {
        if (id == ADMIN_STATIC.toShort()) {
            return ADMIN_STATIC;
        } else if (id == ADMIN_DYNAMIC.toShort()) {
            return ADMIN_DYNAMIC;
        } else {
            return new ControllerID(id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControllerID that = (ControllerID) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
