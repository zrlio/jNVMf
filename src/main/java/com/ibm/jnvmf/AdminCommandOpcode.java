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

class AdminCommandOpcode extends CommandOpcode {
  /*
   * NVMe Spec 1.3a - 5
   */

  static final AdminCommandOpcode GET_LOG_PAGE =
      new AdminCommandOpcode(false, 0, DataTransfer.CONTROLLER_TO_HOST);
  static final AdminCommandOpcode IDENTIFY =
      new AdminCommandOpcode(false, 1, DataTransfer.CONTROLLER_TO_HOST);
  static final AdminCommandOpcode ASYNCHRONOUS_EVENT_REQUEST =
      new AdminCommandOpcode(false, 3, DataTransfer.NO);
  static final AdminCommandOpcode KEEP_ALIVE =
      new AdminCommandOpcode(false, 6, DataTransfer.NO);


  protected AdminCommandOpcode(boolean generic, int function, DataTransfer dataTransfer) {
    super(generic, function, dataTransfer, true);
  }
}
