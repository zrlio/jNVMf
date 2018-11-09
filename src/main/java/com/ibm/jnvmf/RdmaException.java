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

import com.ibm.disni.verbs.IbvWC;

public class RdmaException extends Exception {

  private final IbvWC.IbvWcOpcode opcode;
  private final IbvWC.IbvWcStatus status;

  RdmaException(IbvWC.IbvWcOpcode opcode, IbvWC.IbvWcStatus status) {
    super(opcode.name() + " WC status " + status.ordinal() + ": " + status.name());
    this.status = status;
    this.opcode = opcode;
  }

  public IbvWC.IbvWcStatus getStatus() {
    return this.status;
  }

  public IbvWC.IbvWcOpcode getOpcode() {
    return opcode;
  }

  public static RdmaException fromInteger(int opcode, int status) {
    return new RdmaException(IbvWC.IbvWcOpcode.valueOf(opcode), IbvWC.IbvWcStatus.valueOf(status));
  }
}
