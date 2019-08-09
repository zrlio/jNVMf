package com.ibm.jnvmf;

public class AsynchronousEventInformationNvmCommandSetSpecificStatus
    extends AsynchronousEventInformation {
  public class Value extends AsynchronousEventInformation.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 5.2.1 */
  public final Value RESERVATION_LOG_PAGE_AVAILABLE = new Value(0x0,
      "Indicates that one or more Reservation Notification log pages "
          + "have been added to the Reservation Notification log.");
  public final Value SANITIZE_OPERATION_COMPLETED = new Value(0x1,
      "Indicates that a sanitize operation has completed and status is "
          + "available in the Sanitize Status log page");
  /* 0x2 - 0xff Reserved */

  // CHECKSTYLE_ON: MemberNameCheck

  AsynchronousEventInformationNvmCommandSetSpecificStatus() {
  }

  private static final AsynchronousEventInformationNvmCommandSetSpecificStatus instance =
      new AsynchronousEventInformationNvmCommandSetSpecificStatus();

  public static AsynchronousEventInformationNvmCommandSetSpecificStatus getInstance() {
    return instance;
  }
}
