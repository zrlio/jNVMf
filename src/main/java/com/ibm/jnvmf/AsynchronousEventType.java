package com.ibm.jnvmf;

public class AsynchronousEventType extends EEnum<AsynchronousEventType.Value> {

  public class Value extends EEnum.Value {

    private final String description;
    private final AsynchronousEventInformation asynchronousEventInformation;

    Value(int value, String description,
        AsynchronousEventInformation asynchronousEventInformation) {
      super(value);
      this.description = description;
      this.asynchronousEventInformation = asynchronousEventInformation;
    }

    public String getDescription() {
      return description;
    }

    public AsynchronousEventInformation.Value valueOf(int value) {
      return asynchronousEventInformation.valueOf(value);
    }
  }

  /*
   * NVMe Spec 1.3a - 5.2.1
   *
   */

  // CHECKSTYLE_OFF: MemberNameCheck

  public final Value ERROR_STATUS = new Value(0x0, "Error status",
      AsynchronousEventInformationErrorStatus.getInstance());
  public final Value SMART_HEALTH_STATUS = new Value(0x1, "SMART / Health status",
      AsynchronousEventInformationSmartHealthStatus.getInstance());
  public final Value NOTICE = new Value(0x2, "Notice",
      AsynchronousEventInformationNotice.getInstance());
  /* 0x3 - 0x5 Reserved*/
  public final Value IO_COMMAND_SET_SPECIFIC_STATUS =
      new Value(0x6, "I/O Command Set specific status",
          AsynchronousEventInformationNvmCommandSetSpecificStatus.getInstance());
  public final Value VENDOR_SPECIFIC = new Value(0x7, "Vendor specific",
      AsynchronousEventInformationVendorSpecific.getInstance());

  // CHECKSTYLE_ON: MemberNameCheck

  private AsynchronousEventType() {
    super(7);
  }

  private static final AsynchronousEventType instance = new AsynchronousEventType();

  public static AsynchronousEventType getInstance() {
    return instance;
  }

}
