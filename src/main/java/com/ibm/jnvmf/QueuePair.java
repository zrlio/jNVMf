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

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSendWR;
import com.ibm.disni.verbs.IbvWC;
import com.ibm.disni.verbs.SVCPostSend;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// TODO check valid on public methods

public abstract class QueuePair implements Freeable {

  private static final int POLL_CQ_BATCHSIZE = 32;

  private boolean valid;

  private final Controller controller;
  private final NvmfRdmaEndpoint endpoint;
  private final QueueId queueId;

  private final short submissionQueueSize;
  private final int additionalSgls;

  private RdmaRecv[] rdmaReceives;
  /* Command Identifier -> Response */
  private final Response[] responseMap;

  private KeyedNativeBufferPool commandBufferPool;
  private final int maxCommandCapsuleSize;
  private final int inCapsuleDataSize;
  /* Command Identifier -> Command */
  private final Command[] commandMap;

  private final Queue<Short> freeCommandId;

  private ThreadLocal<NvmfRdmaEndpoint.PollCq> pollCq;

  FabricsConnectResponseCqe connect(long timeout, TimeUnit timeoutUnit) throws IOException {
    NativeBuffer buffer = new NativeByteBuffer(
        ByteBuffer.allocateDirect(RdmaCmRequestPrivateData.SIZE));
    RdmaCmRequestPrivateData privateData = new RdmaCmRequestPrivateData(buffer);
    privateData.setQueueId(queueId);
    privateData.setRdmaQpReceiveQueueSize(submissionQueueSize);
    privateData.setRdmaQpSendQueueSize((short) (submissionQueueSize - 1));
    this.endpoint.getConnParam().setPrivate_data(buffer.getAddress());
    this.endpoint.getConnParam().setPrivate_data_len((byte) RdmaCmRequestPrivateData.SIZE);
    InetSocketAddress socketAddress = controller.getTransportId().getAddress();
    try {
      //FIXME: rejected requests are not handled by DiSNI
      //FIXME: check for overflow
      endpoint.connect(socketAddress, (int)TimeUnit.MILLISECONDS.convert(timeout, timeoutUnit));
    } catch (Exception exception) {
      throw new IOException(exception);
    }
    this.commandBufferPool = endpoint.getBufferPool(maxCommandCapsuleSize);
    this.rdmaReceives = endpoint.getRdmaReceives();
    pollCq = ThreadLocal.withInitial(() -> {
      try {
        return endpoint.new PollCq(POLL_CQ_BATCHSIZE);
      } catch (IOException exception) {
        throw new IllegalStateException(exception);
      }
    });

    /* Send fabrics connect command */
    ByteBuffer dataBuffer = ByteBuffer.allocateDirect(FabricsConnectCommandData.SIZE);
    KeyedNativeBuffer registeredDataBuffer = registerMemory(dataBuffer);
    FabricsConnectCommandData connectCommandData = new FabricsConnectCommandData(
        registeredDataBuffer);

    connectCommandData.setControllerId(controller.getControllerId());
    connectCommandData.setHostNvmeQualifiedName(controller.getHostNvmeQualifiedName());
    connectCommandData.setSubsystemNvmeQualifiedName(controller.getTransportId().getSubsystemNqn());

    FabricsConnectCommand command = new FabricsConnectCommand(this);
    Future<?> commandFuture = command.newCommandFuture();
    FabricsConnectCommandCapsule connectCommand = command.getCommandCapsule();
    connectCommand.setSglDescriptor(connectCommandData);
    FabricsConnectCommandSqe connectCommandSqe = connectCommand.getSubmissionQueueEntry();

    connectCommandSqe.setQueueId(queueId);
    connectCommandSqe.setSubmissionQueueSize(submissionQueueSize);

    ResponseFuture<FabricsConnectResponseCapsule> responseFuture = command.newResponseFuture();
    command.execute(responseFuture);
    FabricsConnectResponseCapsule responseCapsule;
    try {
      commandFuture.get();
      responseCapsule = responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }

    connectCommand.free();
    registeredDataBuffer.free();
    FabricsConnectResponseCqe cqe = responseCapsule.getCompletionQueueEntry();
    if (!cqe.getStatusCode().equals(GenericStatusCode.getInstance().SUCCESS)) {
      throw new UnsuccessfulComandException(cqe);
    } /* TODO invalid parameter exception */
    return cqe;
  }

  QueuePair(Controller controller, QueueId queueId, short submissionQueueSize) throws IOException {
    this(controller, queueId, submissionQueueSize, 0, 0, 0);
  }

  QueuePair(Controller controller, QueueId queueId, short submissionQueueSize,
      int additionalSgls, int inCapsuleDataSize, int maxInlineSize) throws IOException {
    this.controller = controller;
    this.queueId = queueId;
    this.endpoint = controller.getEndpointGroup().createEndpoint();
    this.endpoint.setCqSize(2 * submissionQueueSize);
    this.endpoint.setRqSize(submissionQueueSize);
    this.endpoint.setSqSize(submissionQueueSize);
    this.endpoint.setInlineDataSize(maxInlineSize);
    this.submissionQueueSize = submissionQueueSize;

    this.responseMap = new Response[submissionQueueSize];

    if (additionalSgls < 0) {
      throw new IllegalArgumentException("Additional SGLs negative");
    }
    if (inCapsuleDataSize < 0) {
      throw new IllegalArgumentException("inCapsuleDataSize negative");
    }
    this.inCapsuleDataSize = inCapsuleDataSize;
    if (maxInlineSize < 0) {
      throw new IllegalArgumentException("maxInlineSize negative");
    }
    if (additionalSgls > 0) {
      IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
      /* A value of 0 maximum SGL data block descriptors indicates no limit */
      if ((additionalSgls + 1) > identifyControllerData.getMaximumSglDataBlockDescriptors()
          && identifyControllerData.getMaximumSglDataBlockDescriptors() != 0) {
        throw new IllegalArgumentException("Controller only supports "
            + (identifyControllerData.getMaximumSglDataBlockDescriptors() - 1)
            + " additional SGLs not " + additionalSgls);
      }
    }
    this.additionalSgls = additionalSgls;
    int maxCommandCapsuleSize;
    if (inCapsuleDataSize > 0) {
      IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
      maxCommandCapsuleSize = CommandCapsule.computeCommandCapsuleSize(additionalSgls,
          identifyControllerData.getInCapsuleDataOffset(), inCapsuleDataSize);
      if (maxCommandCapsuleSize > identifyControllerData.getIoQueueCommandCapsuleSupportedSize()) {
        throw new IllegalArgumentException("Command capsule size "
            + maxCommandCapsuleSize + " to large, max supported size is "
            + identifyControllerData.getIoQueueCommandCapsuleSupportedSize());
      }
    } else {
      maxCommandCapsuleSize = CommandCapsule.computeCommandCapsuleSize(additionalSgls, 0, 0);
    }
    this.maxCommandCapsuleSize = maxCommandCapsuleSize;
    this.commandMap = new Command[submissionQueueSize];

    this.freeCommandId = new ArrayBlockingQueue<>(submissionQueueSize);
    for (int i = 0; i < submissionQueueSize; i++) {
      freeCommandId.add((short) i);
    }

    //FIXME: we don't want to introduce yet another argument to the constructor
    // we need to change the way the buffer management works and get rid of
    // additionalSgls and inCapsuleDataSize, then we can introduce a timeout arg
    connect(10, TimeUnit.SECONDS);
    this.valid = true;
  }

  /* we might want to move this to the controller later */
  public KeyedNativeBuffer registerMemory(ByteBuffer buffer) throws IOException {
    IbvMr mr = endpoint.registerMemory(buffer).execute().free().getMr();
    return new RdmaByteBuffer(buffer, mr);
  }

  int getInlineDataSize() {
    return endpoint.getInlineDataSize();
  }

  SVCPostSend newPostSend(List<IbvSendWR> sendWrList) throws IOException {
    return endpoint.postSend(sendWrList);
  }

  KeyedNativeBuffer allocateCommandCapsule() throws IOException {
    return commandBufferPool.allocate();
  }

  private short nextCommandIdentifier() throws IOException {
    Short commandId;
    boolean commandCompleted;
    do {
      commandId = freeCommandId.poll();
      if (commandId == null) {
        throw new IOException("submission queue full");
      }
      commandCompleted = commandMap[commandId] == null;
      if (!commandCompleted) {
        /*
         * response has completed but command has not
         * this should rarely happen
         */
        boolean empty = freeCommandId.isEmpty();
        freeCommandId.add(commandId);
        if (empty) {
          throw new IOException("submission queue full");
        }
      }
    } while (!commandCompleted);
    assert responseMap[commandId] == null;
    return commandId;
  }

  final void post(Command command, SVCPostSend postSend, Response response) throws IOException {
    command.getCallback().onStart();
    response.getCallback().onStart();
    short commandId = nextCommandIdentifier();
    command.setCommandId(commandId);
    responseMap[commandId] = response;
    commandMap[commandId] = command;
    postSend.getWrMod(0).setWr_id(commandId);
    postSend.execute();
  }

  private final void handleSendWc(IbvWC wc) throws IOException {
    int wrId = (int) wc.getWr_id();
    Command command = commandMap[wrId];
    if (command == null) {
      throw new IOException("No command with CID " + wrId);
    }
    commandMap[wrId] = null;
    if (wc.getStatus() != IbvWC.IbvWcStatus.IBV_WC_SUCCESS.ordinal()) {
      command.getCallback().onFailure(RdmaException.fromInteger(wc.getOpcode(), wc.getStatus()));
    } else {
      command.getCallback().onComplete();
    }
  }

  private final void handleReceiveWc(IbvWC wc) throws IOException {
    if (wc.getStatus() != IbvWC.IbvWcStatus.IBV_WC_SUCCESS.ordinal()) {
      /* we don't know to which response this wc responds to so we complete all */
      for (Response response : responseMap) {
        if (response != null) {
          response.getCallback()
              .onFailure(RdmaException.fromInteger(wc.getOpcode(), wc.getStatus()));
        }
      }
      /* TODO should we free all CIDs or terminate the connection? */
    } else {
      RdmaRecv receive = rdmaReceives[(int) wc.getWr_id()];
      KeyedNativeBuffer buffer = receive.getBuffer();
      /* 1) extract CID */
      short commandId = CompletionQueueEntry.getCommandIdentifier(buffer);
      /* 2) remove response and update */
      Response response = responseMap[commandId];
      if (response == null) {
        throw new IOException("No response with CID " + commandId);
      }
      responseMap[commandId] = null;
      response.update(buffer);
      /* 3) Resubmit and add to free receives */
      receive.execute();
      freeCommandId.add(commandId);
      response.getCallback().onComplete();
    }
  }

  public int poll() throws IOException {
    NvmfRdmaEndpoint.PollCq pollCq = this.pollCq.get();
    int polls = pollCq.execute().getPolls();
    IbvWC[] wcs = pollCq.getWorkCompletions();
    for (int i = 0; i < polls; i++) {
      IbvWC wc = wcs[i];
      int opcode = wc.getOpcode();
      if (opcode == IbvWC.IbvWcOpcode.IBV_WC_SEND.getOpcode()) {
        handleSendWc(wc);
      } else if (opcode == IbvWC.IbvWcOpcode.IBV_WC_RECV.getOpcode()) {
        handleReceiveWc(wc);
      }
    }
    return polls;
  }

  public Controller getController() {
    return controller;
  }


  public int getMaximumAdditionalSgls() {
    return additionalSgls;
  }

  @Override
  public void free() throws IOException {
    if (isValid()) {
      valid = false;
      for (RdmaRecv receive : rdmaReceives) {
        receive.free();
      }
      Arrays.fill(commandMap, null);
      Arrays.fill(responseMap, null);
      try {
        endpoint.close();
      } catch (InterruptedException exception) {
        throw new IOException(exception);
      }
    }
  }

  @Override
  public final boolean isValid() {
    return valid;
  }

  public int getInCapsuleDataSize() {
    return inCapsuleDataSize;
  }

  public short getSubmissionQueueSize() {
    return submissionQueueSize;
  }
}

