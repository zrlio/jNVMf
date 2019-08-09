package com.ibm.jnvmf;

public class AsynchronousEventInformationSmartHealthStatus extends AsynchronousEventInformation {

  public class Value extends AsynchronousEventInformation.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 5.2.1 */
  public final Value NVM_SUBSYSTEM_RELIABILITY = new Value(0x0,
      "NVM subsystem reliability has been compromised. This may be due "
          + "to significant media errors, an internal error, the media being placed in "
          + "read only mode, or a volatile memory backup device failing");
  public final Value TEMPERATURE_THRESHOLD = new Value(0x1,
      "A temperature is above an over temperature threshold or below an under "
          + "temperature threshold.");
  public final Value SPARE_BELOW_THRESHOLD = new Value(0x2,
      "Available spare space has fallen below the threshold.");
  /* 0x3 - 0xff Reserved */

  // CHECKSTYLE_ON: MemberNameCheck

  AsynchronousEventInformationSmartHealthStatus() {
  }

  private static final AsynchronousEventInformationSmartHealthStatus instance =
      new AsynchronousEventInformationSmartHealthStatus();

  public static AsynchronousEventInformationSmartHealthStatus getInstance() {
    return instance;
  }

}
