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

public abstract class FabricsPropertyCommandSqe extends FabricsSubmissionQueueEntry {

  /*
   * NVMf Spec 1.0 - 3.4/3.5
   */
  private static int ATTRIBUTES_OFFSET = 40;
  private static int OFFSET_OFFSET = 44;

  FabricsPropertyCommandSqe(NativeBuffer buffer) {
    super(buffer);
  }

  public void setProperty(Property property) {
    getBuffer().put(ATTRIBUTES_OFFSET, property.getSize().toByte());
    getBuffer().putInt(OFFSET_OFFSET, property.getOffset());
  }
}
