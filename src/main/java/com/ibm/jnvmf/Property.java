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

public class Property {

  static class Size extends EEnum<Size.Value> {

    class Value extends EEnum.Value {

      Value(int value) {
        super(value);
      }
    }

    private Size() {
      super(1);
    }

    private static final Size instance = new Size();

    public static Size getInstance() {
      return instance;
    }

    // CHECKSTYLE_OFF: MemberNameCheck

    public final Value FOUR_BYTES = new Value(0);
    public final Value EIGHT_BYTES = new Value(1);

    // CHECKSTYLE_ON: MemberNameCheck
  }

  private final Size.Value size;
  private final int offset;

  Property(Size.Value size, int offset) {
    this.size = size;
    this.offset = offset;
  }

  public Size.Value getSize() {
    return size;
  }

  public int getOffset() {
    return offset;
  }
}
