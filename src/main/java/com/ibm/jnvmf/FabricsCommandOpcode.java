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

public class FabricsCommandOpcode extends CommandOpcode {

  /*
   * NVMf Spec 1.0 - 2.1
   *
   * Set to 0x7F to indicate Fabrics command
   */
  public static final FabricsCommandOpcode FABRIC =
      new FabricsCommandOpcode(false, 0x1F, DataTransfer.BIDIRECTIONAL, false);

  protected FabricsCommandOpcode(boolean generic, int function, DataTransfer dataTransfer,
      boolean admin) {
    super(generic, function, dataTransfer, admin);
  }
}
