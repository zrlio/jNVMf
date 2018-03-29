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

public class ControllerId {

  private final short id;

  public static final ControllerId ADMIN_DYNAMIC = new ControllerId((short) 0xFFFF);
  public static final ControllerId ADMIN_STATIC = new ControllerId((short) 0xFFFE);


  public ControllerId(short id) {
    this.id = id;
  }

  public short toShort() {
    return id;
  }

  public static ControllerId valueOf(short id) {
    if (id == ADMIN_STATIC.toShort()) {
      return ADMIN_STATIC;
    } else if (id == ADMIN_DYNAMIC.toShort()) {
      return ADMIN_DYNAMIC;
    } else {
      return new ControllerId(id);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    ControllerId that = (ControllerId) obj;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) id;
  }
}
