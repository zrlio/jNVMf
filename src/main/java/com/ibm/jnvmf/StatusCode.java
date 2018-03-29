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

public abstract class StatusCode extends EEnum<StatusCode.Value> {

  public class Value extends EEnum.Value {

    private final String description;

    Value(int value, String description) {
      super(value);
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  StatusCode() {
    super(0xff);
  }
}
