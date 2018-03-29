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

public class NvmFusedOperation extends EEnum<NvmFusedOperation.Value> {

  public class Value extends EEnum.Value {

    Value(int value) {
      super(value);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  public final Value NORMAL = new Value(0x0);
  public final Value FIRST_COMMAND = new Value(0x1);
  public final Value SECOND_COMMAND = new Value(0x2);

  // CHECKSTYLE_ON: MemberNameCheck

  private NvmFusedOperation() {
    super(2);
  }

  private static final NvmFusedOperation instance = new NvmFusedOperation();

  public static NvmFusedOperation getInstance() {
    return instance;
  }
}
