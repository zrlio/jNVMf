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

import sun.nio.ch.DirectBuffer;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.util.Objects;

public class NativeByteBuffer implements NativeBuffer {

  private final ByteBuffer buffer;
  private final long address;

  public NativeByteBuffer(ByteBuffer buffer) {
    this.buffer = buffer;
    this.address = ((DirectBuffer) buffer).address();
  }

  protected NativeBuffer construct(ByteBuffer buffer) {
    return new NativeByteBuffer(buffer);
  }

  @Override
  public int position() {
    return buffer.position();
  }

  @Override
  public NativeBuffer position(int newPosition) {
    buffer.position(newPosition);
    return this;
  }

  @Override
  public int limit() {
    return buffer.limit();
  }

  @Override
  public NativeBuffer limit(int newLimit) {
    buffer.limit(newLimit);
    return this;
  }

  @Override
  public int remaining() {
    return buffer.remaining();
  }

  @Override
  public int capacity() {
    return buffer.capacity();
  }

  @Override
  public NativeBuffer clear() {
    buffer.clear();
    return this;
  }

  @Override
  public NativeBuffer flip() {
    buffer.flip();
    return this;
  }

  @Override
  public NativeBuffer slice() {
    return construct(buffer.slice());
  }

  @Override
  public ByteBuffer sliceToByteBuffer() {
    return buffer.slice();
  }

  @Override
  public ByteBuffer toByteBuffer() {
    return buffer;
  }

  @Override
  public ByteOrder order() {
    return buffer.order();
  }

  @Override
  public NativeBuffer order(ByteOrder order) {
    buffer.order(order);
    return this;
  }

  @Override
  public NativeBuffer putShort(int index, short value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException {
    buffer.putShort(index, value);
    return this;
  }

  @Override
  public NativeBuffer putInt(int index, int value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException {
    buffer.putInt(index, value);
    return this;
  }

  @Override
  public NativeBuffer putLong(int index, long value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException {
    buffer.putLong(index, value);
    return this;
  }

  @Override
  public NativeBuffer putLong(long value) throws BufferOverflowException, ReadOnlyBufferException {
    buffer.putLong(value);
    return this;
  }

  @Override
  public NativeBuffer put(int index, byte value)
      throws IndexOutOfBoundsException, ReadOnlyBufferException {
    buffer.put(index, value);
    return this;
  }

  @Override
  public NativeBuffer put(byte value) throws BufferOverflowException, ReadOnlyBufferException {
    buffer.put(value);
    return this;
  }

  @Override
  public NativeBuffer put(ByteBuffer src)
      throws BufferOverflowException, IllegalArgumentException, ReadOnlyBufferException {
    buffer.put(src);
    return this;
  }

  @Override
  public NativeBuffer put(byte[] src) throws BufferOverflowException, ReadOnlyBufferException {
    buffer.put(src);
    return this;
  }

  @Override
  public byte get(int index) throws BufferUnderflowException {
    return buffer.get(index);
  }

  @Override
  public short getShort(int index) throws IndexOutOfBoundsException {
    return buffer.getShort(index);
  }

  @Override
  public int getInt(int index) throws IndexOutOfBoundsException {
    return buffer.getInt(index);
  }

  @Override
  public long getLong(int index) throws IndexOutOfBoundsException {
    return buffer.getLong(index);
  }

  @Override
  public long getAddress() {
    return address;
  }

  @Override
  public void free() throws IOException {
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    NativeByteBuffer that = (NativeByteBuffer) obj;
    return address == that.address && Objects.equals(this.buffer, that.buffer);
  }

  @Override
  public int hashCode() {

    return Objects.hash(buffer, address);
  }
}
