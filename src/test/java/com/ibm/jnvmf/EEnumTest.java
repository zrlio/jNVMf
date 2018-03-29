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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EEnumTest {

  static class TestEnum extends EEnum<TestEnum.Value> {

    TestEnum(int maxValue) {
      super(maxValue);
    }

    class Value extends EEnum<Value>.Value {

      Value(int value) {
        super(value);
      }
    }

  }

  static class TestEnumInherit extends TestEnum {

    class Value extends TestEnum.Value {

      Value(int value) {
        super(value);
      }
    }

    TestEnumInherit(int maxValue) {
      super(maxValue);
    }
  }

  static class OtherTestEnum extends EEnum<TestEnum.Value> {

    OtherTestEnum(int maxValue) {
      super(maxValue);
    }

    class Value extends EEnum<Value>.Value {

      Value(int value) {
        super(value);
      }
    }

  }

  @Test
  void basic() {
    TestEnum test = new TestEnum(0x0);
    TestEnum.Value value = test.new Value(0x0);
    assertEquals(value, test.valueOf(0x0));
  }

  @Test
  void valueOutOfBounds() {
    final TestEnum test = new TestEnum(0x0);
    assertThrows(IllegalArgumentException.class, () -> test.new Value(0x1));
    assertThrows(IllegalArgumentException.class, () -> test.new Value(-1));
  }

  @Test
  void doubleAssignValue() {
    final TestEnum test = new TestEnum(0x0);
    test.new Value(0x0);
    assertThrows(IllegalArgumentException.class, () -> test.new Value(0x0));
  }

  @Test
  void nonConsecutive() {
    final TestEnum test = new TestEnum(128);
    TestEnum.Value value1 = test.new Value(0);
    TestEnum.Value value2 = test.new Value(128);
    assertEquals(value1, test.valueOf(0));
    assertEquals(value2, test.valueOf(128));
    for (int i = 1; i < 128; i++) {
      final int j = i;
      assertThrows(IllegalArgumentException.class, () -> test.valueOf(j));
    }
  }

  @Test
  void toIntByte() {
    TestEnum test = new TestEnum(34);
    int intValue = 1;
    TestEnum.Value value = test.new Value(intValue);
    assertEquals(intValue, value.toInt());
    byte byteValue = 2;
    value = test.new Value(byteValue);
    assertEquals(byteValue, value.toByte());
  }

  @Test
  void equals() {
    TestEnum test1 = new TestEnum(0);
    TestEnum.Value value1 = test1.new Value(0);
    assertEquals(value1, value1);
    TestEnum test2 = new TestEnum(1);
    TestEnum.Value value2 = test2.new Value(0);
    assertEquals(value1, value2);

    TestEnum.Value value3 = test2.new Value(1);
    assertNotEquals(value1, value3);
  }

  @Test
  void inheritanceEqual() {
    TestEnum test1 = new TestEnum(0);
    TestEnum.Value value1 = test1.new Value(0);
    TestEnumInherit test2 = new TestEnumInherit(0);
    TestEnum.Value value2 = test2.new Value(0);
    assertEquals(value1, value2);

    TestEnumInherit test3 = new TestEnumInherit(0);
    TestEnumInherit.Value value3 = test3.new Value(0);
    assertEquals(value1, value3);
    assertEquals(value2, value3);

    OtherTestEnum test4 = new OtherTestEnum(0);
    OtherTestEnum.Value value4 = test4.new Value(0);
    assertNotEquals(value1, value4);
    assertNotEquals(value2, value4);
    assertNotEquals(value3, value4);
  }

  @Test
  void maxValue() {
    assertThrows(IllegalArgumentException.class, () -> new TestEnum(-1));
  }
}