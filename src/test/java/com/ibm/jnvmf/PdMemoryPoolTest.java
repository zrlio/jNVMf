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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ibm.disni.verbs.IbvContext;
import com.ibm.disni.verbs.IbvPd;
import com.ibm.disni.verbs.RdmaCm;
import com.ibm.disni.verbs.RdmaCmEvent;
import com.ibm.disni.verbs.RdmaCmId;
import com.ibm.disni.verbs.RdmaEventChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PdMemoryPoolTest {

  static IbvPd createPd() throws IOException {
    RdmaEventChannel eventChannel = RdmaEventChannel.createEventChannel();
    if (eventChannel == null) {
      throw new IOException("cannot create RDMA evetn channel");
    }

    RdmaCmId cmId = eventChannel.createId(RdmaCm.RDMA_PS_TCP);
    if (cmId == null) {
      throw new IOException("cannot create CM id");
    }

    InetAddress address = InetAddress.getByName(TestUtil.getLocalAddress());
    InetSocketAddress socketAddress = new InetSocketAddress(address, 12345);
    cmId.resolveAddr(null, socketAddress, 2000);

    RdmaCmEvent cmEvent = eventChannel.getCmEvent(-1);
    if (cmEvent == null) {
      throw new IOException("no CM event");
    } else if (cmEvent.getEvent() != RdmaCmEvent.EventType.RDMA_CM_EVENT_ADDR_RESOLVED.ordinal()) {
      throw new IOException("Invalid state: " + cmEvent.getEvent());
    }
    cmEvent.ackEvent();

    cmId.resolveRoute(2000);

    cmEvent = eventChannel.getCmEvent(-1);
    if (cmEvent == null) {
      throw new IOException("no CM event");
    } else if (cmEvent.getEvent() != RdmaCmEvent.EventType.RDMA_CM_EVENT_ROUTE_RESOLVED.ordinal()) {
      throw new IOException("Invalid state: " + cmEvent.getEvent());
    }
    cmEvent.ackEvent();

    IbvContext context = cmId.getVerbs();
    return context.allocPd();
  }


  @Test
  void argumentCheck() {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(null, memoryAllocator, 1, 1, 1, ByteOrder.LITTLE_ENDIAN));
  }

  @Tag("rdma")
  @Test
  void argumentCheckRdma() throws IOException {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    IbvPd pd = createPd();
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, 0, 1, 1, ByteOrder.LITTLE_ENDIAN));
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, -1, 1, 1, ByteOrder.LITTLE_ENDIAN));
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, 1, 0, 1, ByteOrder.LITTLE_ENDIAN));
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, 1, -1, 1, ByteOrder.LITTLE_ENDIAN));
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, 1, 1, 0, ByteOrder.LITTLE_ENDIAN));
    assertThrows(IllegalArgumentException.class,
        () -> new PdMemoryPool(pd, memoryAllocator, 1, 1, -1, ByteOrder.LITTLE_ENDIAN));
  }

  @Tag("rdma")
  @Test
  void element() throws IOException {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    IbvPd pd = createPd();
    int size = 512;
    final PdMemoryPool memoryPool =
        new PdMemoryPool(pd, memoryAllocator, size, 1, 1, ByteOrder.LITTLE_ENDIAN);
    final KeyedNativeBuffer buffer = memoryPool.allocate();
    assertEquals(size, buffer.capacity());
    assertEquals(size, buffer.remaining());
    assertEquals(0, buffer.position());
    assertTrue(buffer.isValid());
    assertThrows(OutOfMemoryError.class, () -> memoryPool.allocate());
    ThreadLocalRandom random = ThreadLocalRandom.current();
    byte b[] = new byte[size - 128];
    random.nextBytes(b);
    buffer.put(b);
    buffer.free();
    assertFalse(buffer.isValid());
    assertThrows(IllegalStateException.class, () -> buffer.free());

    KeyedNativeBuffer buffer2 = memoryPool.allocate();
    assertEquals(size, buffer2.capacity());
    assertEquals(size, buffer2.remaining());
    assertEquals(0, buffer2.position());
    assertTrue(buffer2.isValid());
    assertEquals(ByteOrder.LITTLE_ENDIAN, buffer2.order());
  }

  @Tag("rdma")
  @Test
  void elementEndianess() throws IOException {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    IbvPd pd = createPd();
    int size = 512;
    PdMemoryPool memoryPool =
        new PdMemoryPool(pd, memoryAllocator, size, 1, 1, ByteOrder.BIG_ENDIAN);
    KeyedNativeBuffer buffer = memoryPool.allocate();
    assertEquals(ByteOrder.BIG_ENDIAN, buffer.order());

    memoryPool =
        new PdMemoryPool(pd, memoryAllocator, size, 1, 1, ByteOrder.LITTLE_ENDIAN);
    buffer = memoryPool.allocate();
    assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.free();
    buffer = memoryPool.allocate();
    assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
  }

  @Tag("rdma")
  @Test
  void elementSlice() throws IOException {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    IbvPd pd = createPd();
    int size = 512;
    PdMemoryPool memoryPool =
        new PdMemoryPool(pd, memoryAllocator, size, 1, 1, ByteOrder.BIG_ENDIAN);
    KeyedNativeBuffer buffer = memoryPool.allocate();
    int newLimit = 128;
    int newPosition = 16;
    int newSize = newLimit - newPosition;
    buffer.position(newPosition);
    buffer.limit(newLimit);
    NativeBuffer slicedBuffer = buffer.slice();
    assertTrue(slicedBuffer.isValid());
    assertEquals(newSize, slicedBuffer.capacity());
    assertEquals(newSize, slicedBuffer.remaining());
    assertEquals(0, slicedBuffer.position());
    slicedBuffer.free();
    assertFalse(slicedBuffer.isValid());
    assertFalse(buffer.isValid());

    buffer = memoryPool.allocate();
    assertEquals(size, buffer.capacity());
    assertEquals(size, buffer.remaining());
    assertEquals(0, buffer.position());
    assertTrue(buffer.isValid());
  }

  @Tag("rdma")
  @Test
  void elementSize() throws IOException {
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();
    IbvPd pd = createPd();
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 10; i++) {
      int size = random.nextInt(1, 16 * 1024 * 1024);
      PdMemoryPool memoryPool =
          new PdMemoryPool(pd, memoryAllocator, size, 1, 1, ByteOrder.LITTLE_ENDIAN);
      KeyedNativeBuffer buffer = memoryPool.allocate();
      assertEquals(size, buffer.capacity());
      assertEquals(size, buffer.remaining());
      assertEquals(0, buffer.position());
      assertTrue(buffer.isValid());
      assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
    }
  }

  @Tag("rdma")
  @Test
  void region() throws IOException {
    final int size = 512;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    IbvPd pd = createPd();
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();

    for (int i = 0; i < 10; i++) {
      int elementsRegion = random.nextInt(1, 256);
      PdMemoryPool memoryPool =
          new PdMemoryPool(pd, memoryAllocator, size, elementsRegion, 1, ByteOrder.LITTLE_ENDIAN);
      KeyedNativeBuffer buffers[] = new KeyedNativeBuffer[elementsRegion];
      for (int j = 0; j < elementsRegion; j++) {
        buffers[j] = memoryPool.allocate();
      }
      for (int j = 0; j < random.nextInt(1, 256); j++) {
        assertThrows(OutOfMemoryError.class, () -> memoryPool.allocate());
      }
      for (int j = 0; j < elementsRegion; j++) {
        buffers[j].free();
      }
      for (int j = 0; j < elementsRegion; j++) {
        buffers[j] = memoryPool.allocate();
      }
    }

    for (int i = 0; i < 10; i++) {
      int regions = random.nextInt(1, 256);
      PdMemoryPool memoryPool =
          new PdMemoryPool(pd, memoryAllocator, size, 1, regions, ByteOrder.LITTLE_ENDIAN);
      KeyedNativeBuffer buffers[] = new KeyedNativeBuffer[regions];
      for (int j = 0; j < regions; j++) {
        buffers[j] = memoryPool.allocate();
      }
      for (int j = 0; j < random.nextInt(1, 256); j++) {
        assertThrows(OutOfMemoryError.class, () -> memoryPool.allocate());
      }
      for (int j = 0; j < regions; j++) {
        buffers[j].free();
      }
      for (int j = 0; j < regions; j++) {
        buffers[j] = memoryPool.allocate();
      }
    }
  }

  @Tag("rdma")
  @Test
  void multiThreadedAllocate() throws Exception {
    final int size = 512;
    final int elementsRegion = 2048;
    final int regions = 128;
    IbvPd pd = createPd();
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();

    final int totalElements = elementsRegion * regions;
    int numThreads[] = new int[]{2, 4, 8, 16};
    KeyedNativeBuffer buffers[] = new KeyedNativeBuffer[totalElements];
    for (int n : numThreads) {
      PdMemoryPool memoryPool =
          new PdMemoryPool(pd, memoryAllocator, size, elementsRegion, regions,
              ByteOrder.LITTLE_ENDIAN);
      Thread threads[] = new Thread[n];
      Exception es[] = new Exception[n];
      int perThread = totalElements / threads.length;
      for (int i = 0; i < threads.length; i++) {
        final int x = i;
        threads[i] = new Thread(() -> {
          try {
            for (int k = 0; k < perThread; k++) {
              buffers[perThread * x + k] = memoryPool.allocate();
            }
          } catch (Exception e) {
            es[x] = e;
          }
        });
      }
      for (Thread thread : threads) {
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join();
      }
      for (Exception e : es) {
        if (e != null) {
          System.err.println("numThreads = " + numThreads);
          throw e;
        }
      }
      for (KeyedNativeBuffer buffer : buffers) {
        assertTrue(buffer != null);
      }
      assertThrows(OutOfMemoryError.class, () -> memoryPool.allocate());
      for (int i = 0; i < buffers.length; i++) {
        buffers[i] = null;
      }
    }
  }

  @Tag("rdma")
  @Test
  void multiThreadedAllocateFree() throws Exception {
    final int size = 512;
    final int elementsRegion = 2048;
    final int regions = 128;
    IbvPd pd = createPd();
    MemoryAllocator memoryAllocator = new OffHeapMemoryAllocator();

    final int totalElements = elementsRegion * regions;
    PdMemoryPool memoryPool =
        new PdMemoryPool(pd, memoryAllocator, size, elementsRegion, regions,
            ByteOrder.LITTLE_ENDIAN);

    Queue<KeyedNativeBuffer> buffers = new ArrayBlockingQueue<>(totalElements);
    Exception es[] = new Exception[totalElements];
    Thread allocate = new Thread(() -> {
      for (int i = 0; i < totalElements; i++) {
        try {
          buffers.add(memoryPool.allocate());
        } catch (Exception e) {
          es[i] = e;
        }
      }
    });
    Thread free = new Thread(() -> {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      int nWait = random.nextInt(10, 1000);
      while (buffers.size() < nWait) {
        ;
      }
      for (int i = 0; i < totalElements; i++) {
        KeyedNativeBuffer buffer;
        do {
          buffer = buffers.poll();
        } while (buffer == null);
        try {
          buffer.free();
        } catch (Exception e) {
          es[i] = e;
        }
      }
    });
    free.start();
    allocate.start();

    allocate.join();
    free.join();

    for (Exception e : es) {
      if (e != null) {
        throw e;
      }
    }
    assertEquals(0, buffers.size());
    for (int i = 0; i < totalElements; i++) {
      buffers.add(memoryPool.allocate());
    }
    assertEquals(totalElements, buffers.size());
    assertThrows(OutOfMemoryError.class, () -> memoryPool.allocate());
  }
}