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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BitUtilTest {

  private String print(long value, int index) {
    return "value = " + Long.toHexString(value) + ", index = " + index;
  }

  private String print(int value, int index) {
    return "value = " + Integer.toHexString(value) + ", index = " + index;
  }

  @Test
  void getBit() {
    int x = 1;
    assertTrue(BitUtil.getBit(x, 0));
    for (int i = 1; i < Integer.SIZE; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    x = 3;
    assertTrue(BitUtil.getBit(x, 0));
    assertTrue(BitUtil.getBit(x, 1));
    for (int i = 2; i < Integer.SIZE; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    x = 0x10;
    for (int i = 0; i < Integer.SIZE; i++) {
      if (i == 4) {
        assertTrue(BitUtil.getBit(x, i), print(x, i));
      } else {
        assertTrue(!BitUtil.getBit(x, i), print(x, i));
      }
    }

    x = 0x90;
    for (int i = 0; i < Integer.SIZE; i++) {
      if (i == 4 || i == 7) {
        assertTrue(BitUtil.getBit(x, i), print(x, i));
      } else {
        assertTrue(!BitUtil.getBit(x, i), print(x, i));
      }
    }

    x = 1 << (Integer.SIZE - 1);
    assertTrue(BitUtil.getBit(x, Integer.SIZE - 1));
    for (int i = 0; i < Integer.SIZE - 1; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBit(~0, -2));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBit(~0, Integer.SIZE));
  }

  @Test
  void getBitLong() {
    long x = 1;
    assertTrue(BitUtil.getBit(x, 0));
    for (int i = 1; i < Long.SIZE; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    x = 3;
    assertTrue(BitUtil.getBit(x, 0));
    assertTrue(BitUtil.getBit(x, 1));
    for (int i = 2; i < Long.SIZE; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    x = 0x10;
    for (int i = 0; i < Long.SIZE; i++) {
      if (i == 4) {
        assertTrue(BitUtil.getBit(x, i), print(x, i));
      } else {
        assertTrue(!BitUtil.getBit(x, i), print(x, i));
      }
    }

    x = 0x90;
    for (int i = 0; i < Long.SIZE; i++) {
      if (i == 4 || i == 7) {
        assertTrue(BitUtil.getBit(x, i), print(x, i));
      } else {
        assertTrue(!BitUtil.getBit(x, i), print(x, i));
      }
    }

    x = 1L << (Long.SIZE - 1);
    assertTrue(BitUtil.getBit(x, Long.SIZE - 1));
    for (int i = 0; i < Long.SIZE - 1; i++) {
      assertTrue(!BitUtil.getBit(x, i), print(x, i));
    }

    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBit(~0L, -1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBit(~0L, Long.SIZE));
  }

  @Test
  void clearBit() {
    int x = 0xf;
    x = BitUtil.clearBit(x, 0);
    assertEquals(0xe, x);
    x = BitUtil.clearBit(x, 1);
    assertEquals(0xc, x);
    x = BitUtil.clearBit(x, 1);
    assertEquals(0xc, x);
    x = BitUtil.clearBit(x, 2);
    assertEquals(0x8, x);
    x = BitUtil.clearBit(x, 3);
    assertEquals(0, x);

    x = 1 << (Integer.SIZE - 1);
    assertEquals(0, BitUtil.clearBit(x, Integer.SIZE - 1));

    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBit(~0, -5));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBit(~0, Integer.SIZE));
  }

  @Test
  void setBit() {
    int x = 0x1f;
    x = BitUtil.setBit(x, 5);
    assertEquals(0x3f, x);
    x = BitUtil.setBit(x, 5);
    assertEquals(0x3f, x);
    x = 0;
    x = BitUtil.setBit(x, Integer.SIZE - 1);
    assertEquals(1 << (Integer.SIZE - 1), x);

    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBit(0, -1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBit(0, Integer.SIZE));
  }

  @Test
  void setBitTo() {
    int x = 0xf;
    assertEquals(BitUtil.setBit(x, 4), BitUtil.setBitTo(x, 4, true));
    assertEquals(BitUtil.clearBit(x, 0), BitUtil.setBitTo(x, 0, false));

    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitTo(0, -1, true));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitTo(0, Integer.SIZE, false));
  }

  @Test
  void getBits() {
    int x = 0xc0ffee;
    assertEquals(0xee, BitUtil.getBits(x, 0, 7));
    assertEquals(0x0f, BitUtil.getBits(x, 12, 19));
    assertEquals(0x7, BitUtil.getBits(x, 1, 4));
    x = 1 << (Integer.SIZE - 1);
    assertEquals(0x80, BitUtil.getBits(x, 24, 31));

    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0, 0, Integer.SIZE));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0, 1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0, 0, -1));
    assertThrows(IllegalArgumentException.class,
        () -> BitUtil.getBits(~0, Integer.SIZE, Integer.SIZE));
  }

  @Test
  void getBitsLong() {
    long x = 0xc0ffeeL;
    assertEquals(0xee, BitUtil.getBits(x, 0, 7));
    assertEquals(0x0f, BitUtil.getBits(x, 12, 19));
    assertEquals(0x7, BitUtil.getBits(x, 1, 4));
    assertEquals(0, BitUtil.getBits(x, 32, 63));
    x = 1L << (Long.SIZE - 1);
    assertEquals(0x80, BitUtil.getBits(x, 56, 63));

    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0L, 0, Long.SIZE));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0L, 1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0L, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0L, 0, -1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.getBits(~0L, Long.SIZE, Long.SIZE));
  }

  @Test
  void clearBits() {
    int x = 0xc0ffee;
    x = BitUtil.clearBits(x, 0, 3);
    assertEquals(0xc0ffe0, x);
    x = BitUtil.clearBits(x, 0, 3);
    assertEquals(0xc0ffe0, x);
    x = BitUtil.clearBits(x, 12, 19);
    assertEquals(0xc00fe0, x);
    x = BitUtil.clearBits(x, 13, 22);
    assertEquals(0x800fe0, x);
    x = BitUtil.clearBits(x, 0, Integer.SIZE - 1);
    assertEquals(0, x, Integer.toHexString(x));

    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBits(~0, 0, Integer.SIZE));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBits(~0, 1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBits(~0, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.clearBits(~0, 0, -1));
    assertThrows(IllegalArgumentException.class,
        () -> BitUtil.clearBits(~0, Integer.SIZE, Integer.SIZE));
  }

  @Test
  void setBitsTo() {
    int x = 0;
    x = BitUtil.setBitsTo(x, 1, 4, 0xf);
    assertEquals(0x1e, x);
    x = BitUtil.setBitsTo(x, 31, 31, 1);
    assertEquals(0x8000001e, x);
    x = BitUtil.setBitsTo(x, 31, 31, 0);
    assertEquals(0x1e, x);

    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitsTo(0, 0, Integer.SIZE, 1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitsTo(0, 1, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitsTo(0, -1, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitsTo(0, 0, -1, 1));
    assertThrows(IllegalArgumentException.class,
        () -> BitUtil.setBitsTo(0, Integer.SIZE, Integer.SIZE, 1));
    assertThrows(IllegalArgumentException.class, () -> BitUtil.setBitsTo(0, 0, 1, 0xf));
  }

}