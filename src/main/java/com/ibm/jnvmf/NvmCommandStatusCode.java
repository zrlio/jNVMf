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

public class NvmCommandStatusCode extends CommandSpecificStatusCode {

  public class Value extends CommandSpecificStatusCode.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 4.6.1.2.2 Figure 34 */
  public final Value CONFLICTING_ATTRIBUTES = new Value(0x80,
      "The attributes specified in the command are conflicting.");
  public final Value INVALID_PROTECTION_INFORMATION = new Value(0x81,
      "The Protection Information Field (PRINFO) "
          + "settings specified in the command are invalid for the Protection Information with "
          + "which the namespace was formatted or the EILBRT/ILBRT field is invalid.");
  public final Value ATTEMTED_WRITE_TO_READ_ONLY_RANGE = new Value(0x82,
      "The LBA range specified contains read-only blocks.");

  // CHECKSTYLE_ON: MemberNameCheck

  private NvmCommandStatusCode() {
  }

  private static final NvmCommandStatusCode instance = new NvmCommandStatusCode();

  public static NvmCommandStatusCode getInstance() {
    return instance;
  }
}
