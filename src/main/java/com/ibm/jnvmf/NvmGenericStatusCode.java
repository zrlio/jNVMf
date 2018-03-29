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

public class NvmGenericStatusCode extends GenericStatusCode {

  public class Value extends GenericStatusCode.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  public final Value LBA_OUT_OF_RANGE = new Value(0x80,
      "The command references an LBA that exceeds the size of the namespace.");
  public final Value CAPACITY_EXCEEDED = new Value(0x81,
      "Execution of the command has caused the capacity of the namespace to be exceeded.");
  public final Value NAMESPACE_NOT_READY = new Value(0x82,
      "The namespace is not ready to be accessed. The Do Not Retry bit "
          + "indicates whether re-issuing the command at a later time may succeed.");
  public final Value RESERVATION_CONFLICT = new Value(0x83,
      "The command was aborted due to a conflict with a reservation held on the "
          + "accessed namespace.");
  public final Value FORMAT_IN_PROGRESS = new Value(0x84,
      " A Format NVM command is in progress on the namespace. The Do Not Retry bit shall "
          + "be cleared to ‘0’ to indicate that the command may succeed if it is resubmitted.");

  // CHECKSTYLE_ON: MemberNameCheck


  private NvmGenericStatusCode() {
  }

  private static final NvmGenericStatusCode instance = new NvmGenericStatusCode();

  public static NvmGenericStatusCode getInstance() {
    return instance;
  }
}
