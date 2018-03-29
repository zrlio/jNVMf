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

public class AdminIdentifyCommandReturnType extends EEnum<AdminIdentifyCommandReturnType.Value> {

  public class Value extends EEnum.Value {

    Value(int value) {
      super(value);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  public final Value NAMESPACE = new Value(0x0);
  public final Value CONTROLLER = new Value(0x1);
  public final Value ACTIVE_NAMESPACE_IDS = new Value(0x2);
  public final Value NAMESPACE_DESCRIPTORS = new Value(0x3);
  public final Value NAMESPACE_IDS = new Value(0x10);

  // CHECKSTYLE_ON: MemberNameCheck

  private AdminIdentifyCommandReturnType() {
    super(0x15);
  }

  private static final AdminIdentifyCommandReturnType instance =
      new AdminIdentifyCommandReturnType();

  public static AdminIdentifyCommandReturnType getInstance() {
    return instance;
  }
}
