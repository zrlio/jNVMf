package com.ibm.jnvmf;

public class ErrorInformationLogEntry extends NativeData<KeyedNativeBuffer> {

  /*
   * NVMe Spec 1.3 - 5.14.1.1
   *
   * 00:07 Error Count
   * 08:09 Submission Queue ID
   * 10:11 Command ID
   * 12:13 Status Field
   * 14:15 Parameter Error Location
   * 16:23 LBA
   * 24:27 Namespace
   * 28:28 Vendor Specific Information Available
   * 29:31 Reserved
   * 32:39 Command Specific Information
   * 40:63 Reserved
   */

  public static final int SIZE = 64;

  private static final int ERROR_COUNT_OFFSET = 0;
  private static final int SUBMISSION_QUEUE_ID_OFFSET = 8;
  private static final int COMMAND_ID_OFFSET = 10;
  private static final int STATUS_FIELD_OFFSET = 12;
  private static final int PARAMETER_ERROR_LOCATION_OFFSET = 14;
  private static final int PARAMETER_ERROR_BITLOCATION_OFFSET = 15;
  private static final int LBA_OFFSET = 16;
  private static final int NAMESPACE_OFFSET = 24;
  private static final int VENDOR_SPECIFIC_INFORMATION_AVAILABLE_OFFSET = 28;
  private static final int COMMAND_SPECIFIC_INFORMATION_OFFSET = 32;

  private final StatusField statusField;

  ErrorInformationLogEntry(KeyedNativeBuffer buffer) {
    super(buffer, SIZE);
    this.statusField = new StatusField(STATUS_FIELD_OFFSET);
  }

  /* unique error identifier */
  public class ErrorCount {
    private final long value;

    private ErrorCount() {
      this.value = getBuffer().getLong(ERROR_COUNT_OFFSET);
    }

    public boolean isValid() {
      return value != 0;
    }

    public long getValue() {
      return value;
    }
  }

  public ErrorCount getErrorCount() {
    return new ErrorCount();
  }

  public QueueId getSubmissionQueueId() {
    return new QueueId(getBuffer().getShort(SUBMISSION_QUEUE_ID_OFFSET));
  }

  public short getCommandId() {
    return getBuffer().getShort(COMMAND_ID_OFFSET);
  }

  public StatusField getStatusField() {
    statusField.update(getBuffer());
    return statusField;
  }

  public class ParameterErrorLocation {

    /*
     * 00:07 Byte location
     * 08:10 Bit location
     * 11:15 Reserved
     */
    private static final int BIT_LOCATION_BITOFFSET_START = 0;
    private static final int BIT_LOCATION_BITOFFSET_END = 2;

    private final int byteLocation;
    private final int bitLocation;

    private ParameterErrorLocation() {
      this.byteLocation = getBuffer().get(PARAMETER_ERROR_LOCATION_OFFSET);
      this.bitLocation = BitUtil.getBits(getBuffer().get(PARAMETER_ERROR_BITLOCATION_OFFSET),
          BIT_LOCATION_BITOFFSET_START, BIT_LOCATION_BITOFFSET_END);
    }

    public int getByteLocation() {
      if (!isValid()) {
        throw new IllegalStateException(
            "The error is not specific to a particular command parameter.");
      }
      return byteLocation;
    }

    public int getBitLocation() {
      return bitLocation;
    }

    public boolean isValid() {
      return byteLocation <= 63 && byteLocation >= 0;
    }
  }

  public ParameterErrorLocation getParameterErrorLocation() {
    return new ParameterErrorLocation();
  }

  public long getLba() {
    return getBuffer().getLong(LBA_OFFSET);
  }

  public NamespaceIdentifier getNamespaceIdentifier() {
    return new NamespaceIdentifier(getBuffer().getInt(NAMESPACE_OFFSET));
  }

  public boolean isVendorSpecificInformationAvailable() {
    int logPageRaw = getBuffer().get(VENDOR_SPECIFIC_INFORMATION_AVAILABLE_OFFSET);
    return logPageRaw >= 0x80 && logPageRaw <= 0xFF;
  }

  public LogPageIdentifier getVendorSpecificInformationLogPage() {
    int logPageRaw = getBuffer().get(VENDOR_SPECIFIC_INFORMATION_AVAILABLE_OFFSET);
    if (!isVendorSpecificInformationAvailable()) {
      throw new IllegalStateException("No vendor specific information available");
    }
    return LogPageIdentifier.valueOf((byte)logPageRaw);
  }

  public long getCommandSpecificInformation() {
    return getBuffer().getLong(COMMAND_SPECIFIC_INFORMATION_OFFSET);
  }

  @Override
  void initialize() { }
}
