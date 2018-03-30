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

public class FabricsConnectResponseCqe extends FabricsCompletionQueueEntry {

  private final Success success;
  private final InvalidParameter invalidParameter;

  FabricsConnectResponseCqe() {
    this.success = new Success();
    this.invalidParameter = new InvalidParameter();
  }

  public Success success() {
    StatusCode.Value statusCode = getStatusCode();
    if (statusCode != GenericStatusCode.getInstance().SUCCESS) {
      throw new IllegalStateException("Fabrics connect command was not successful but was\n"
          + "Code: " + statusCode.toInt() + " - " + statusCode.getDescription());
    }
    return success;
  }

  public InvalidParameter invalidParameter() {
    StatusCode.Value statusCode = getStatusCode();
    if (statusCode != FabricsCommandStatusCode.getInstance().CONNECT_INVALID_PARAMETERS) {
      throw new IllegalStateException("Status code was not invalid parameter but\n"
          + "Code: " + statusCode.toInt() + " - " + statusCode.getDescription());
    }
    return invalidParameter;
  }

  @Override
  void update(NativeBuffer buffer) {
    super.update(buffer);
    StatusCode.Value statusCode = getStatusCode();
    if (statusCode == GenericStatusCode.getInstance().SUCCESS) {
      success.update(buffer);
    } else if (statusCode == FabricsCommandStatusCode.getInstance().CONNECT_INVALID_PARAMETERS) {
      invalidParameter.update(buffer);
    }
  }

  static class Success extends Updatable<NativeBuffer> {
    /*
     * NVMf Spec 1.0 - 3.3 Figure 22
     *
     * 01:00 Controller ID
     * 03:02 Authentication Requirements
     *
     */

    private static final int CONTROLLER_ID_OFFSET = 0;
    private ControllerId controllerId;

    private static final int AUTHENTICATION_REQUIREMENTS_OFFSET = 2;
    private boolean tcgSecurityProtocols;


    ControllerId getControllerId() {
      return controllerId;
    }

    boolean usesTcgSecurityProtocols() {
      return tcgSecurityProtocols;
    }

    @Override
    void update(NativeBuffer buffer) {
      short contId = buffer.getShort(CONTROLLER_ID_OFFSET);
      controllerId = ControllerId.valueOf(contId);
      int auth = buffer.get(AUTHENTICATION_REQUIREMENTS_OFFSET);
      tcgSecurityProtocols = BitUtil.getBit(auth, 0);
    }
  }

  static class InvalidParameter extends Updatable<NativeBuffer> {

    /*
     * NVMf Spec 1.0 - 3.3 Figure 22
     *
     * 01:00 Invalid Parameter Offset
     * 02 Invalid Attributes
     *
     */
    private static final int INVALID_PARAMETER_OFFSET = 0;
    private static final int INVALID_ATTRIBUTES = 2;

    short getInvalidParameterOffset() {
      return (short) 0;
    }

    @Override
    void update(NativeBuffer buffer) {
      //buffer.getShort(INVALID_PARAMETER_OFFSET);
      //TODO
    }
  }
}
