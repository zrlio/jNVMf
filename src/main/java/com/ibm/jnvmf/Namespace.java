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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Namespace {

  // TODO: A namespace can have multiple controllers for fault tolerance etc
  private final Controller controller;
  private final NamespaceIdentifier namespaceIdentifier;
  private final IdentifyNamespaceData identifyNamespaceData;

  private final AdminIdentifyNamespaceCommand command;

  Namespace(Controller controller, NamespaceIdentifier namespaceIdentifier) throws IOException {
    this.controller = controller;
    this.namespaceIdentifier = namespaceIdentifier;
    ByteBuffer buffer = ByteBuffer.allocateDirect(IdentifyNamespaceData.SIZE);
    KeyedNativeBuffer registeredBuffer = controller.getAdminQueue().registerMemory(buffer);
    this.identifyNamespaceData = new IdentifyNamespaceData(registeredBuffer);
    this.command = new AdminIdentifyNamespaceCommand(controller.getAdminQueue());
    AdminIdentifyNamespaceCommandCapsule commandCapsule = command.getCommandCapsule();
    commandCapsule.setSglDescriptor(identifyNamespaceData);
    AdminIdentifyCommandSqe sqe = commandCapsule.getSubmissionQueueEntry();
    sqe.setNamespaceIdentifier(namespaceIdentifier);
  }

  public boolean isActive() {
    //TODO
    return false;
  }

  public NamespaceIdentifier getIdentifier() {
    return namespaceIdentifier;
  }

  public Controller getController() {
    return controller;
  }

  private void updateIdentifyNamespaceData() throws IOException {
    Future<?> commandFuture = command.newCommandFuture();
    ResponseFuture<AdminResponseCapsule> responseFuture = command.newResponseFuture();
    command.execute(responseFuture);
    try {
      commandFuture.get();
      responseFuture.get();
    } catch (InterruptedException exception) {
      throw new IOException(exception);
    } catch (ExecutionException exception) {
      throw new IOException(exception);
    }
  }

  public IdentifyNamespaceData getIdentifyNamespaceData() throws IOException {
    updateIdentifyNamespaceData();
    return identifyNamespaceData;
  }
}
