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

import com.ibm.disni.verbs.IbvSendWR;
import com.ibm.disni.verbs.IbvSge;
import com.ibm.disni.verbs.SVCPostSend;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public abstract class Command<C extends CommandCapsule, R extends ResponseCapsule> extends
    Operation {

  private final QueuePair queuePair;
  private final C commandCapsule;
  private final IbvSendWR wr;
  private SVCPostSend postSend;

  Command(QueuePair queuePair, C commandCapsule) {
    this.queuePair = queuePair;
    this.commandCapsule = commandCapsule;
    KeyedNativeBuffer buffer = commandCapsule.getBuffer();
    IbvSge sge = new IbvSge();
    sge.setAddr(buffer.getAddress());
    sge.setLength(buffer.remaining());
    sge.setLkey(buffer.getLocalKey());

    LinkedList<IbvSge> sgList = new LinkedList<>();
    sgList.add(sge);
    this.wr = new IbvSendWR();
    wr.setSg_list(sgList);
    wr.setNum_sge(1);
    wr.setOpcode(IbvSendWR.IbvWrOcode.IBV_WR_SEND.ordinal());
    wr.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
  }

  public void setSendInline(boolean sendInline) {
    if (sendInline) {
      LinkedList<IbvSge> sgList = wr.getSg_list();
      long size = 0;
      for (IbvSge sge : sgList) {
        size += sge.getLength();
      }
      if (getQueuePair().getInlineDataSize() >= size) {
        wr.setSend_flags(wr.getSend_flags() | IbvSendWR.IBV_SEND_INLINE);
      } else {
        throw new IllegalArgumentException("Insufficient inline data size");
      }
    } else {
      wr.setSend_flags(wr.getSend_flags() & ~IbvSendWR.IBV_SEND_INLINE);
    }
  }

  private SVCPostSend getPostSend() throws IOException {
    if (postSend == null) {
      postSend = getQueuePair().newPostSend(Arrays.asList(wr));
    }
    return postSend;
  }

  public Response<R> execute(Response<R> response) throws IOException {
    getQueuePair().post(this, getPostSend(), response);
    return response;
  }

  public ResponseFuture<R> execute(ResponseFuture<R> responseFuture) throws IOException {
    getQueuePair().post(this, getPostSend(), responseFuture.getOperation());
    return responseFuture;
  }

  public abstract Response<R> newResponse();

  public ResponseFuture<R> newResponseFuture() {
    return new ResponseFuture<>(getQueuePair(), newResponse());
  }

  public CommandFuture<C, R> newCommandFuture() {
    return new CommandFuture<>(getQueuePair(), this);
  }

  void setCommandId(short commandId) {
    commandCapsule.getSubmissionQueueEntry().setCommandIdentifier(commandId);
  }

  public QueuePair getQueuePair() {
    return queuePair;
  }

  public C getCommandCapsule() {
    return commandCapsule;
  }
}
