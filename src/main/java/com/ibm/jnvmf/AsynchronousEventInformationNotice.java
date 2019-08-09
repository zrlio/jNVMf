package com.ibm.jnvmf;

public class AsynchronousEventInformationNotice extends AsynchronousEventInformation {

  public class Value extends AsynchronousEventInformation.Value {

    Value(int value, String description) {
      super(value, description);
    }
  }

  // CHECKSTYLE_OFF: MemberNameCheck

  /* NVM Spec 1.3a - 5.2.1 */
  public final Value NAMESPACE_ATTRIBUTE_CHANGED = new Value(0x0,
      "The Identify Namespace data structure for one or more namespaces, "
          + "as well as the Namespace List returned when the Identify command is issued with "
          + "the CNS field set to 02h, have changed. Host software may use this event as an "
          + "indication that it should read the Identify Namespace data structures for each "
          + "namespace to determine what has changed.\n"
          + "Alternatively, host software may request the Changed Namespace List "
          + "(Log Identifier 04h) to determine which namespaces in this controller have "
          + "changed Identify Namespace information since the last time the log page was read.");
  public final Value FIRMWARE_ACTIVATION_STARTING = new Value(0x1,
      "The controller is starting a firmware activation process during "
          + "which command processing is paused. Host software may use CSTS.PP to determine when "
          + "command processing has resumed. To clear this event, host software reads the "
          + "Firmware Slot Information log page.");
  public final Value TELEMETRY_LOG_CHANGED = new Value(0x2,
      "The controller has saved the controller internal state in the Telemetry "
          + "Controller-Initiated log page and set the Telemetry Controller-Initiated Data "
          + "Available field to 1h in that log page. To clear this event, the host issues "
          + "a Get Log Page with Retain Asynchronous Event cleared to ‘0’ for the "
          + "Telemetry Controller-Initiated Log.");
  /* 0x3 - 0xff Reserved */

  // CHECKSTYLE_ON: MemberNameCheck

  AsynchronousEventInformationNotice() {
  }

  private static final AsynchronousEventInformationNotice instance =
      new AsynchronousEventInformationNotice();

  public static AsynchronousEventInformationNotice getInstance() {
    return instance;
  }
}
