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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;

/* Since we cannot extend ByteBuffer we use our own interface */
public interface NativeBuffer extends Freeable {

  int position();

  NativeBuffer position(int newPosition);

  int limit();

  NativeBuffer limit(int newLimit);

  int remaining();

  int capacity();

  NativeBuffer clear();

  NativeBuffer flip();

  NativeBuffer slice();

  ByteBuffer sliceToByteBuffer();

  ByteBuffer toByteBuffer();

  ByteOrder order();

  NativeBuffer order(ByteOrder order);

  NativeBuffer putShort(int index, short value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException;

  NativeBuffer putInt(int index, int value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException;

  NativeBuffer putLong(int index, long value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException;

  NativeBuffer putLong(long value) throws BufferOverflowException, ReadOnlyBufferException;

  NativeBuffer put(int index, byte value) throws IndexOutOfBoundsException, ReadOnlyBufferException;

  NativeBuffer put(byte value) throws BufferOverflowException, ReadOnlyBufferException;

  NativeBuffer put(ByteBuffer src)
      throws BufferOverflowException, IllegalArgumentException, ReadOnlyBufferException;

  NativeBuffer put(byte[] src) throws BufferOverflowException, ReadOnlyBufferException;

  byte get(int index) throws IndexOutOfBoundsException;

  short getShort(int index) throws IndexOutOfBoundsException;

  int getInt(int index) throws IndexOutOfBoundsException;

  long getLong(int index) throws IndexOutOfBoundsException;

  long getAddress();
}
