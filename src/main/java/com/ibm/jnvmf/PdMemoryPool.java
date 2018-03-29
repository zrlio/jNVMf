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

import com.ibm.disni.rdma.verbs.IbvMr;
import com.ibm.disni.rdma.verbs.IbvPd;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class PdMemoryPool implements KeyedNativeBufferPool, Freeable {

  private final IbvPd protectionDomain;
  private final MemoryAllocator allocator;
  private final ByteOrder endianness;
  private final int elementSize;
  private final int numElementsRegion;
  private final MemoryRegion[] regions;
  private int numFreeRegions;

  private final Queue<KeyedNativeBuffer> freeElements;

  private class MemoryRegion {

    private final NativeBuffer buffer;
    private final IbvMr mr;

    private MemoryRegion(NativeBuffer buffer) throws IOException {
      this.buffer = buffer;
      /* TODO: allow to create memory pools for different access types */
      int access = IbvMr.IBV_ACCESS_LOCAL_WRITE
          | IbvMr.IBV_ACCESS_REMOTE_READ
          | IbvMr.IBV_ACCESS_REMOTE_WRITE;
      this.mr = protectionDomain.regMr(buffer.toByteBuffer(), access).execute().free().getMr();
      ;
    }

    private void free() throws IOException {
      mr.deregMr().execute().free();
      buffer.free();
    }

    private PdMemoryPool getOuter() {
      return PdMemoryPool.this;
    }
  }

  private static class Element extends NativeByteBuffer implements KeyedNativeBuffer {

    private final MemoryRegion region;
    private boolean valid;

    private Element(ByteBuffer buffer, MemoryRegion region) {
      super(buffer);
      this.region = region;
      this.valid = true;
    }

    private Element(Element element) {
      this(element.toByteBuffer(), element.region);
      clear();
    }

    @Override
    public int getRemoteKey() {
      return region.mr.getRkey();
    }

    @Override
    public int getLocalKey() {
      return region.mr.getLkey();
    }

    @Override
    public void free() {
      if (!isValid()) {
        throw new IllegalStateException("double free buffer");
      }
      valid = false;
      region.getOuter().free(this);
    }

    @Override
    public boolean isValid() {
      return valid;
    }

    @Override
    protected KeyedNativeBuffer construct(ByteBuffer buffer) {
      return new ChildElement(this, buffer);
    }
  }

  private static class ChildElement extends NativeByteBuffer implements KeyedNativeBuffer {

    private final Element parent;

    ChildElement(Element parent, ByteBuffer buffer) {
      super(buffer);
      this.parent = parent;
    }

    @Override
    public int getRemoteKey() {
      return parent.getRemoteKey();
    }

    @Override
    public int getLocalKey() {
      return parent.getLocalKey();
    }


    @Override
    public void free() {
      parent.free();
    }

    @Override
    public boolean isValid() {
      return parent.isValid();
    }
  }

  PdMemoryPool(IbvPd protectionDomain, MemoryAllocator allocator,
      int elementSize, int numElementsRegion, int numRegions, ByteOrder endianness) {
    if (protectionDomain == null) {
      throw new IllegalArgumentException("Protection domain null");
    }
    this.protectionDomain = protectionDomain;
    if (allocator == null) {
      throw new IllegalArgumentException("Allocator null");
    }
    this.allocator = allocator;
    this.endianness = endianness;
    if (elementSize < 1) {
      throw new IllegalArgumentException("Negative or zero element size");
    }
    this.elementSize = elementSize;
    if (numElementsRegion < 1) {
      throw new IllegalArgumentException("Negative or zero number of freeElements per region");
    }
    this.numElementsRegion = numElementsRegion;
    if (numRegions < 1) {
      throw new IllegalArgumentException("Negative or zero number of regions");
    }
    this.regions = new MemoryRegion[numRegions];
    this.numFreeRegions = numRegions;
    this.freeElements = new ArrayBlockingQueue<>(numElementsRegion * numRegions);
  }

  synchronized void allocateRegion() throws IOException {
    if (!freeElements.isEmpty()) {
      return;
    }
    if (numFreeRegions > 0) {
      NativeBuffer regionBuffer = allocator.allocate(elementSize * numElementsRegion);
      if (regions[numFreeRegions - 1] != null) {
        regionBuffer.free();
        throw new RuntimeException("There is already a region allocated in this spot!?");
      }

      MemoryRegion region = new MemoryRegion(regionBuffer);
      for (int i = 0; i < numElementsRegion; i++) {
        regionBuffer.limit((i + 1) * elementSize);
        regionBuffer.position(i * elementSize);
        ByteBuffer buffer = regionBuffer.sliceToByteBuffer();
        buffer.order(endianness);
        freeElements.add(new Element(buffer, region));
      }
      regions[--numFreeRegions] = region;
    } else {
      throw new OutOfMemoryError("Cannot allocate new region - limit reached");
    }
  }

  public KeyedNativeBuffer allocate() throws IOException {
    KeyedNativeBuffer element;
    do {
      if (freeElements.isEmpty()) {
        allocateRegion();
      }
      element = freeElements.poll();
    } while (element == null);
    return element;
  }

  private void free(Element element) {
    /* we create a new element since we don't want it to get valid again */
    Element newElement = new Element(element);
    newElement.order(endianness);
    freeElements.add(newElement);
  }

  @Override
  public void free() throws IOException {
    for (int i = regions.length - 1; i >= numFreeRegions; i--) {
      if (regions[i] == null) {
        break;
      }
      regions[i].free();
      regions[i] = null;
    }
    numFreeRegions = regions.length;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  public IbvPd getProtectionDomain() {
    return protectionDomain;
  }
}
