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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller implements Freeable {

  private short queueId;
  private final AdminQueuePair adminQueue;
  private final List<IoQueuePair> ioQueuePairs;
  private final NvmeQualifiedName hostNvmeQualifiedName;
  private final NvmfTransportId transportId;
  private final NvmfRdmaEndpointGroup endpointGroup;
  private ControllerId controllerId;
  private final ControllerConfiguration controllerConfiguration;
  private final ControllerStatus controllerStatus;
  private final ControllerCapabilities controllerCapabilities;
  private final List<Namespace> namespaces;
  private boolean valid;

  private final AdminKeepAliveCommand keepAliveCommand;
  private final FabricsPropertySetCommand propertySetCommand;
  private final FabricsPropertyGetCommand propertyGetCommand;
  private final AdminIdentifyActiveNamespaceIdsCommand activeNamespaceIdsCommand;
  private NamespaceIdentifierList namespaceIdentifierList;

  private IdentifyControllerData identifyControllerData;

  Controller(NvmeQualifiedName hostNvmeQualifiedName, NvmfTransportId transportId,
      long connectTimeout, TimeUnit connectTimeoutUnit, boolean dynamicId) throws IOException {
    this.hostNvmeQualifiedName = hostNvmeQualifiedName;
    this.transportId = transportId;
    this.queueId = 1;
    this.endpointGroup = new NvmfRdmaEndpointGroup(connectTimeout, connectTimeoutUnit);
    this.endpointGroup.init(new NvmfRdmaEndpointFactory(endpointGroup));

    if (dynamicId) {
      setControllerId(ControllerId.ADMIN_DYNAMIC);
    } else {
      setControllerId(ControllerId.ADMIN_STATIC);
    }
    this.adminQueue = new AdminQueuePair(this);
    this.ioQueuePairs = new ArrayList<>();

    this.keepAliveCommand = new AdminKeepAliveCommand(getAdminQueue());
    this.propertySetCommand = new FabricsPropertySetCommand(getAdminQueue());
    this.propertyGetCommand = new FabricsPropertyGetCommand(getAdminQueue());
    this.activeNamespaceIdsCommand = new AdminIdentifyActiveNamespaceIdsCommand(getAdminQueue());

    this.controllerConfiguration = new ControllerConfiguration();
    this.controllerCapabilities = new ControllerCapabilities();
    controllerCapabilities.update(getProperty(ControllerCapabilities.PROPERTY));
    this.controllerStatus = new ControllerStatus();

    this.namespaces = new ArrayList<>();
    valid = true;
  }

  private IdentifyControllerData identifyController() throws IOException {
    ByteBuffer dataBuffer = ByteBuffer.allocateDirect(IdentifyControllerData.SIZE);
    KeyedNativeBuffer registeredDataBuffer = adminQueue.registerMemory(dataBuffer);
    IdentifyControllerData identifyControllerData = new IdentifyControllerData(registeredDataBuffer,
        controllerCapabilities);
    AdminIdentifyControllerCommand identifyControllerCommand = new AdminIdentifyControllerCommand(
        adminQueue);
    identifyControllerCommand.getCommandCapsule().setSglDescriptor(identifyControllerData);

    ResponseFuture<AdminResponseCapsule> responseFuture = identifyControllerCommand
        .newResponseFuture();
    Future<?> commandFuture = identifyControllerCommand.newCommandFuture();
    identifyControllerCommand.execute(responseFuture);
    AdminResponseCapsule response;
    try {
      commandFuture.get();
      response = responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }
    AdminCompletionQueueEntry cqe = response.getCompletionQueueEntry();
    if (cqe.getStatusCode() != GenericStatusCode.getInstance().SUCCESS) {
      throw new UnsuccessfulComandException(cqe);
    }
    return identifyControllerData;
  }

  public IdentifyControllerData getIdentifyControllerData() throws IOException {
    if (identifyControllerData == null) {
      identifyControllerData = identifyController();
    }
    return identifyControllerData;
  }

  public AdminQueuePair getAdminQueue() {
    return adminQueue;
  }

  private QueueId nextQueueId() {
    return new QueueId(queueId++);
  }

  public IoQueuePair createIoQueuePair(int submissionQueueSize) throws IOException {
    return createIoQueuePair(submissionQueueSize, 0, 0, 0);
  }

  public IoQueuePair createIoQueuePair(int submissionQueueSize, int additionalSgls,
      int inCapsuleDataSize,
      int maxInlineSize) throws IOException {
    if ((submissionQueueSize & ~((1 << Short.SIZE) - 1)) != 0) {
      throw new IllegalArgumentException("Size to large (" + submissionQueueSize + ")");
    }
    /* only update controller status if we need to */
    if (!controllerStatus.isReady() && !getControllerStatus().isReady()) {
      throw new IllegalStateException("Controller not ready - enable controller");
    }
    getControllerConfiguration();
    if (controllerConfiguration.getIoSubmissionQueueEntrySize().value() == 0
        || controllerConfiguration.getIoCompletionQueueEntrySize().value() == 0) {
      controllerConfiguration.setIoSubmissionQueueEntrySize(
          getIdentifyControllerData().getRequiredSubmissionQueueEntrySize());
      controllerConfiguration.setIoCompletionQueueEntrySize(
          getIdentifyControllerData().getRequiredCompletionQueueEntrySize());
      syncConfiguration();
    }
    IoQueuePair ioQueuePair = new IoQueuePair(this, nextQueueId(), (short) submissionQueueSize,
        additionalSgls, inCapsuleDataSize, maxInlineSize);
    ioQueuePairs.add(ioQueuePair);
    return ioQueuePair;
  }

  private void updateNamespacesIdentfiers() throws IOException {
    AdminIdentifyActiveNamespacesCommandCapsule commandCapsule = activeNamespaceIdsCommand
        .getCommandCapsule();
    if (namespaceIdentifierList == null) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(NamespaceIdentifierList.SIZE);
      KeyedNativeBuffer registeredBuffer = getAdminQueue().registerMemory(buffer);
      namespaceIdentifierList = new NamespaceIdentifierList(registeredBuffer);

      commandCapsule.setSglDescriptor(namespaceIdentifierList);
      AdminIdentifyCommandSqe sqe = commandCapsule.getSubmissionQueueEntry();
      sqe.setNamespaceIdentifier(new NamespaceIdentifier(0));
    }
    Future<?> commandFuture = activeNamespaceIdsCommand.newCommandFuture();
    ResponseFuture<AdminResponseCapsule> responseFuture = activeNamespaceIdsCommand
        .newResponseFuture();
    activeNamespaceIdsCommand.execute(responseFuture);
    try {
      commandFuture.get();
      AdminResponseCapsule response = responseFuture.get();
      AdminCompletionQueueEntry cqe = response.getCompletionQueueEntry();
      if (cqe.getStatusCode() != GenericStatusCode.getInstance().SUCCESS) {
        throw new UnsuccessfulComandException(cqe);
      }
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }

    namespaces.clear();
    NamespaceIdentifier namespaceId;
    for (int i = 0; (namespaceId = namespaceIdentifierList.getIdentifier(i)) != null; i++) {
      namespaces.add(new Namespace(this, namespaceId));
    }
  }

  public List<Namespace> getActiveNamespaces() throws IOException {
    updateNamespacesIdentfiers();
    return namespaces;
  }

  public void keepAlive() throws IOException {
    Future<?> commandFuture = keepAliveCommand.newCommandFuture();
    ResponseFuture<AdminResponseCapsule> responseFuture = keepAliveCommand.newResponseFuture();
    keepAliveCommand.execute(responseFuture);
    AdminResponseCapsule responseCapsule;
    try {
      commandFuture.get();
      responseCapsule = responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }
    AdminCompletionQueueEntry cqe = responseCapsule.getCompletionQueueEntry();
    if (cqe.getStatusCode() != GenericStatusCode.getInstance().SUCCESS) {
      throw new UnsuccessfulComandException(cqe);
    }
  }

  private void setProperty(Property property, long value) throws IOException {
    FabricsPropertySetCommandCapsule propertySetCommandCapsule = propertySetCommand
        .getCommandCapsule();
    propertySetCommandCapsule.getSubmissionQueueEntry().setProperty(property);
    propertySetCommandCapsule.getSubmissionQueueEntry().setValue(value);

    Future<?> commandFuture = propertySetCommand.newCommandFuture();
    ResponseFuture<FabricsResponseCapsule> responseFuture = propertySetCommand.newResponseFuture();
    propertySetCommand.execute(responseFuture);
    FabricsResponseCapsule responseCapsule;
    try {
      commandFuture.get();
      responseCapsule = responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }
    FabricsCompletionQueueEntry cqe = responseCapsule.getCompletionQueueEntry();
    if (cqe.getStatusCode() != GenericStatusCode.getInstance().SUCCESS) {
      throw new UnsuccessfulComandException(cqe);
    }
  }

  private long getProperty(Property property) throws IOException {
    FabricsPropertyGetCommandCapsule propertyGetCommandCapsule = propertyGetCommand
        .getCommandCapsule();
    propertyGetCommandCapsule.getSubmissionQueueEntry().setProperty(property);

    Future<?> commandFuture = propertyGetCommand.newCommandFuture();
    ResponseFuture<FabricsPropertyGetResponseCapsule> responseFuture = propertyGetCommand
        .newResponseFuture();
    propertyGetCommand.execute(responseFuture);
    FabricsPropertyGetResponseCapsule responseCapsule;
    try {
      commandFuture.get();
      responseCapsule = responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }
    FabricsPropertyGetResponseCqe cqe = responseCapsule.getCompletionQueueEntry();
    if (cqe.getStatusCode() != GenericStatusCode.getInstance().SUCCESS) {
      throw new UnsuccessfulComandException(cqe);
    }
    return cqe.getValue();
  }

  public void syncConfiguration() throws IOException {
    if (LegacySupport.ENABLED) {
      LegacySupport.initializeControllerConfiguration(controllerConfiguration);
    }
    setProperty(controllerConfiguration.PROPERTY, controllerConfiguration.toInt());
  }

  public void waitUntilReady() throws IOException, TimeoutException {
    if (!getControllerConfiguration().getEnable()) {
      throw new IllegalStateException("Controller not enabled");
    }
    long maxWaitDuration = TimeUnit.NANOSECONDS.convert(getControllerCapabilities().getTimeout(),
        ControllerCapabilities.getTimeoutUnit());
    long maxWaitTime = System.nanoTime() + maxWaitDuration;
    long sleepTime = TimeUnit.MILLISECONDS.convert(getControllerCapabilities().getTimeout(),
        ControllerCapabilities.getTimeoutUnit()) / 10;
    ControllerStatus controllerStatus;
    while (!(controllerStatus = getControllerStatus()).isReady()) {
      if (controllerStatus.isFatalStatus()) {
        throw new IOException("Fatal controller error");
      }
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException exception) {
        throw new IOException(exception);
      }
      if (maxWaitTime > System.nanoTime()) {
        throw new TimeoutException("Controller did not become ready in "
            + getControllerCapabilities().getTimeout() + ControllerCapabilities.getTimeoutUnit());
      }
    }
  }

  public NvmfTransportId getTransportId() {
    return transportId;
  }

  NvmfRdmaEndpointGroup getEndpointGroup() {
    return endpointGroup;
  }

  void setControllerId(ControllerId controllerId) {
    this.controllerId = controllerId;
  }

  public ControllerId getControllerId() {
    return controllerId;
  }

  @Override
  public void free() throws IOException {
    valid = false;
    try {
      keepAliveCommand.getCommandCapsule().free();
      propertySetCommand.getCommandCapsule().free();
      propertyGetCommand.getCommandCapsule().free();
      activeNamespaceIdsCommand.getCommandCapsule().free();
    } catch (Exception exception) {
      throw new IOException(exception);
    }
    if (identifyControllerData != null) {
      identifyControllerData.getBuffer().free();
    }
    adminQueue.free();
    for (QueuePair qp : ioQueuePairs) {
      qp.free();
    }
    try {
      endpointGroup.close();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    }
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  public ControllerConfiguration getControllerConfiguration() throws IOException {
    controllerConfiguration.update((int) getProperty(ControllerConfiguration.PROPERTY));
    return controllerConfiguration;
  }

  public ControllerStatus getControllerStatus() throws IOException {
    controllerStatus.update((int) getProperty(ControllerStatus.PROPERTY));
    return controllerStatus;
  }

  public ControllerCapabilities getControllerCapabilities() {
    return controllerCapabilities;
  }

  public NvmeQualifiedName getHostNvmeQualifiedName() {
    return hostNvmeQualifiedName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Controller that = (Controller) obj;
    return Objects.equals(controllerId, that.controllerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(controllerId);
  }
}
