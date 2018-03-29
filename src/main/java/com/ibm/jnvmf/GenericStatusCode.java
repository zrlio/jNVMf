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

public class GenericStatusCode extends StatusCode {

  public class Value extends StatusCode.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 4.6.1.2.1 */
  public final Value SUCCESS =
      new Value(0x0, "The command completed successfully.");
  public final Value INVALID_COMMAND_OPCODE =
      new Value(0x1, "The associated command opcode field is not valid.");
  public final Value INVALID_FIELD =
      new Value(0x2,
          "A reserved coded value or an unsupported value in a defined field"
              + "(other than opcode field)");
  public final Value COMMAND_ID_CONFLICT =
      new Value(0x3, "The command identifier is already in use.");
  public final Value DATA_TRANSFER_ERROR =
      new Value(0x4, "Transferring the data or metadata associated with a command "
          + "had an error.");

  public final Value INVALID_NAMESPACE_OR_FORMAT =
      new Value(0xb, " The namespace or the format of that namespace is invalid.");
  public final Value COMMAND_SEQUENCE_ERROR =
      new Value(0xc, "The command was aborted due to a protocol violation in a"
          + "multicommand sequence (e.g. a violation of the Security Send and Security Receive "
          + "sequencing rules in the TCG Storage Synchronous Interface Communications protocol).");

  public final Value DATA_SGL_LENGTH_INVALID =
      new Value(0xf, "This may occur if the length of a Data SGL is too short."
          + "This may occur if the length of a Data SGL is too long and the controller does not "
          + "support SGL transfers longer than the amount of data to be transferred as indicated "
          + "in the SGL Support field of the Identify Controller data structure.");

  // CHECKSTYLE_ON: MemberNameCheck

  GenericStatusCode() {
  }

  private static final GenericStatusCode instance = new GenericStatusCode();

  public static GenericStatusCode getInstance() {
    return instance;
  }
}
