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

public class CommandSpecificStatusCode extends StatusCode {

  public class Value extends StatusCode.Value {
    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 4.6.1.2.2 */
  public final Value COMPLETION_QUEUE_INVALID = new Value(0x0,
      "The Completion Queue identifier specified in the command does not exist.");
  public final Value INVALID_QUEUE_IDENTIFIER = new Value(0x1,
      "The Queue Identifier specified in the command is invalid or currently in use.");
  public final Value INVALID_QUEUE_SIZE = new Value(0x2,
      " The host attempted to create a queue with an invalid number of entries");
  public final Value ABORT_COMMAND_LIMIT_EXCEEDED = new Value(0x3,
      ": The number of concurrently outstanding Abort commands has "
          + "exceeded the limit indicated in the Identify Controller data structure.");
  public final Value ASYNCHRONOUS_EVENT_REQUEST_LIMIT_EXCEEDED = new Value(0x5,
                "The number of concurrently outstanding "
                    + "Asynchronous Event Request commands has been exceeded.");

  // CHECKSTYLE_ON: MemberNameCheck
}
