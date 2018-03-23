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

class BitUtil {

    private static void checkBounds(int n, int limit) {
        if (n < 0) {
            throw new IllegalArgumentException("negative index " + n);
        }
        if (n >= limit) {
            throw new IllegalArgumentException("index too large " + n + ">=" + limit);
        }
    }

    static boolean getBit(int value, int n) {
        checkBounds(n, Integer.SIZE);
        return (value & (1 << n)) == 0 ? false : true;
    }

    static boolean getBit(long value, int n) {
        checkBounds(n, Long.SIZE);
        return (value & (1L << n)) == 0 ? false : true;
    }

    static int clearBit(int value, int n) {
        checkBounds(n, Integer.SIZE);
        return value & ~(1 << n);
    }

    static int setBit(int value, int n) {
        checkBounds(n, Integer.SIZE);
        return value | (1 << n);
    }

    static int setBitTo(int value, int n, boolean b) {
        if (b) {
            return setBit(value, n);
        } else {
            return clearBit(value, n);
        }
    }

    private static int getMask(int start, int end) {
        checkBounds(start, Integer.SIZE);
        checkBounds(end, Integer.SIZE);
        if (start > end) {
            throw new IllegalArgumentException("start index exceeds end");
        }
        return (int) ((1L << (end + 1 - start)) - 1L);
    }

    /* [start, end] (inclusive) */
    static int getBits(int value, int start, int end) {
        int mask = getMask(start, end);
        value = value >> start;
        return value & mask;
    }

    private static long getMaskLong(int start, int end) {
        checkBounds(start, Long.SIZE);
        checkBounds(end, Long.SIZE);
        if (start > end) {
            throw new IllegalArgumentException("start index exceeds end");
        }
        return (1L << (end + 1 - start)) - 1L;
    }

    /* [start, end] (inclusive) */
    static long getBits(long value, int start, int end) {
        long mask = getMaskLong(start, end);
        value = value >> start;
        return value & mask;
    }

    static int clearBits(int value, int start, int end) {
        int mask = getMask(start, end);
        return value & ~(mask << start);
    }

    /* [start, end] (inclusive) */
    static int setBitsTo(int value, int start, int end, int setTo) {
        value = clearBits(value, start, end);
        int mask = getMask(start, end);
        if (setTo != (setTo & mask)) {
            throw new IllegalArgumentException(
                    Integer.toHexString(setTo) + " does not fit inside " + start + ":" + end);
        }
        return value | (setTo << start);
    }
}
