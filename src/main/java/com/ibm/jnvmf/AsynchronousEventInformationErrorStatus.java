package com.ibm.jnvmf;

public class AsynchronousEventInformationErrorStatus extends AsynchronousEventInformation {

  public class Value extends AsynchronousEventInformation.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 5.2.1 */
  public final Value WRITE_TO_INVALID_DOORBELL_REGISTER = new Value(0x0,
      "Host software wrote the doorbell of a queue that was not created.");
  public final Value INVALID_DOORBELL_WRITE_VALUE = new Value(0x1,
      "Host software attempted to write an invalid doorbell value.");
  public final Value DIAGNOSTIC_FAILURE = new Value(0x2,
      "A diagnostic failure was detected. This may include a self test operation.");
  public final Value PERSISTENT_INTERNAL_ERROR = new Value(0x3,
      "A failure occurred that is persistent and the controller is unable to isolate "
          + "to a specific set of commands. If this error is indicated, then the CSTS.CFS bit "
          + "may be set to ‘1’ and the host should perform a reset.");
  public final Value TRANSIENT_INTERNAL_ERROR = new Value(0x4,
      "A transient error occurred that is specific to a particular set of commands; "
          + "controller operation may continue without a reset.");
  public final Value FIRMWARE_IMAGE_LOAD_ERROR = new Value(0x5,
      "The firmware image could not be loaded. The controller reverted to "
          + "the previously active firmware image or a baseline read-only firmware image.");
  /* 0x6 - 0xff Reserved */

  // CHECKSTYLE_ON: MemberNameCheck

  AsynchronousEventInformationErrorStatus() {
  }

  private static final AsynchronousEventInformationErrorStatus instance =
      new AsynchronousEventInformationErrorStatus();

  public static AsynchronousEventInformationErrorStatus getInstance() {
    return instance;
  }
}
