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

import com.ibm.disni.rdma.RdmaCqProvider;
import com.ibm.disni.rdma.RdmaEndpointGroup;
import com.ibm.disni.rdma.verbs.IbvCQ;
import com.ibm.disni.rdma.verbs.IbvPd;
import com.ibm.disni.rdma.verbs.IbvQP;
import com.ibm.disni.rdma.verbs.IbvQPInitAttr;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class NvmfRdmaEndpointGroup extends RdmaEndpointGroup<NvmfRdmaEndpoint> {

  static class BufferPoolKey {

    private final IbvPd pd;
    private final int size;


    BufferPoolKey(IbvPd pd, int size) {
      this.pd = pd;
      this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      BufferPoolKey that = (BufferPoolKey) obj;

      if (size != that.size) {
        return false;
      }
      return pd.equals(that.pd);
    }

    @Override
    public int hashCode() {
      int result = size;
      result = 31 * result + pd.hashCode();
      return result;
    }
  }

  private final Map<BufferPoolKey, PdMemoryPool> bufferPools;

  public NvmfRdmaEndpointGroup(int timeout) throws IOException {
    super(timeout);
    this.bufferPools = new ConcurrentHashMap<>();
  }

  @Override
  public RdmaCqProvider createCqProvider(NvmfRdmaEndpoint endpoint) throws IOException {
    return new RdmaCqProvider(endpoint.getIdPriv().getVerbs(), endpoint.getCqSize());
  }

  @Override
  public IbvQP createQpProvider(NvmfRdmaEndpoint endpoint) throws IOException {
    IbvQPInitAttr attr = new IbvQPInitAttr();
    attr.cap().setMax_recv_sge(1);
    attr.cap().setMax_recv_wr(endpoint.getRqSize());
    //FIXME: we should support multiple sge for incapsule data if not in the same buffer
    attr.cap().setMax_send_sge(1);
    attr.cap().setMax_send_wr(endpoint.getSqSize());
    attr.cap().setMax_inline_data(endpoint.getInlineDataSize());
    attr.setQp_type(IbvQP.IBV_QPT_RC);
    RdmaCqProvider cqProvider = endpoint.getCqProvider();
    IbvCQ cq = cqProvider.getCQ();
    attr.setRecv_cq(cq);
    attr.setSend_cq(cq);
    IbvQP qp = endpoint.getIdPriv().createQP(endpoint.getPd(), attr);
    if (qp == null) {
      throw new IOException("Create QP failed");
    }
    return qp;
  }

  @Override
  public void allocateResources(NvmfRdmaEndpoint endpoint) throws Exception {
    endpoint.allocateResources();
  }

  KeyedNativeBufferPool getBufferPool(NvmfRdmaEndpoint endpoint, int size) throws IOException {
    BufferPoolKey key = new BufferPoolKey(endpoint.getPd(), size);
    PdMemoryPool bufferPool = bufferPools.get(key);
    if (bufferPool == null) {
      //FIXME size, memory allocator
      bufferPool = new PdMemoryPool(endpoint.getPd(), new OffHeapMemoryAllocator(),
          size, 128, 128, ByteOrder.LITTLE_ENDIAN);
      PdMemoryPool prevCommandBufferPool = bufferPools.putIfAbsent(key, bufferPool);
      if (prevCommandBufferPool != null) {
        bufferPool.free();
        bufferPool = prevCommandBufferPool;
      }
    }
    return bufferPool;
  }
}
