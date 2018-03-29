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

abstract class ScatterGatherListDescriptor extends NativeData<NativeBuffer> {

  public static final int SIZE = 16;
  private static final int IDENTIFIER_OFFSET = 15;
  private static final int TYPE_BITOFFSET = 4;

  abstract static class SubType extends EEnum<SubType.Value> {

    SubType() {
      super(0xf);
    }

    class Value extends EEnum.Value {

      Value(int value) {
        super(value);
      }
    }
  }

  static class Type extends EEnum<Type.Value> {

    class Value extends EEnum.Value {

      Value(int value) {
        super(value);
      }
    }

    // CHECKSTYLE_OFF: MemberNameCheck

    public final Value SGL_DATABLOCK = new Value(0x0);

    public final Value KEYED_SGL_DATABLOCK = new Value(0x4);

    // CHECKSTYLE_ON: MemberNameCheck

    private Type() {
      super(0xf);
    }

    private static final Type instance = new Type();

    public static Type getInstance() {
      return instance;
    }
  }

  ScatterGatherListDescriptor(NativeBuffer buffer) {
    super(buffer, SIZE);
  }

  protected final void setIdentifier(Type.Value type, SubType.Value subType) {
    int identifier = subType.toInt();
    identifier = identifier | (type.toInt() << TYPE_BITOFFSET);
    getBuffer().put(IDENTIFIER_OFFSET, (byte) identifier);
  }
}
