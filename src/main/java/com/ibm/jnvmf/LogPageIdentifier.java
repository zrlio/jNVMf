package com.ibm.jnvmf;

public class LogPageIdentifier {

  /*
   * NVMe Spec 1.3a - 5.14.1
   *
   */

  private final byte id;

  /* 0x0 reserved */
  public static final LogPageIdentifier ERROR_INFORMATION =
      new LogPageIdentifier((byte)0x1);
  public static final LogPageIdentifier SMART_HEALTH_INFORMATION =
      new LogPageIdentifier((byte)0x2);
  public static final LogPageIdentifier FIRMWARE_SLOT_INFORMATION =
      new LogPageIdentifier((byte)0x3);
  public static final LogPageIdentifier CHANGED_NAMESPACE_LIST =
      new LogPageIdentifier((byte)0x4);
  public static final LogPageIdentifier COMMANDS_SUPPORTED_AND_EFFECTS =
      new LogPageIdentifier((byte)0x5);
  public static final LogPageIdentifier DEVICE_SELF_TEST =
      new LogPageIdentifier((byte)0x6);
  public static final LogPageIdentifier TELEMETRY_HOST_INITIATED =
      new LogPageIdentifier((byte)0x7);
  public static final LogPageIdentifier TELEMETRY_CONTROLLER_INITIATED =
      new LogPageIdentifier((byte)0x8);
  /* 0x9 - 0x6F reserved */
  public static final LogPageIdentifier DISCOVERY =
      new LogPageIdentifier((byte)0x70);
  /* 0x71 - 0x7F NVMf (reserved) */
  /* 0x80 - 0xBF I/O Command Set Specific */
  /* 0xC0 - 0xFF Vendor Specific */

  LogPageIdentifier(byte id) {
    this.id = id;
  }

  public byte toByte() {
    return id;
  }

  public static LogPageIdentifier valueOf(byte id) {
    if (id == ERROR_INFORMATION.toByte()) {
      return ERROR_INFORMATION;
    } else if (id == SMART_HEALTH_INFORMATION.toByte()) {
      return SMART_HEALTH_INFORMATION;
    } else if (id == FIRMWARE_SLOT_INFORMATION.toByte()) {
      return FIRMWARE_SLOT_INFORMATION;
    } else if (id == CHANGED_NAMESPACE_LIST.toByte()) {
      return CHANGED_NAMESPACE_LIST;
    } else if (id == COMMANDS_SUPPORTED_AND_EFFECTS.toByte()) {
      return COMMANDS_SUPPORTED_AND_EFFECTS;
    } else if (id == DEVICE_SELF_TEST.toByte()) {
      return DEVICE_SELF_TEST;
    } else if (id == TELEMETRY_HOST_INITIATED.toByte()) {
      return TELEMETRY_HOST_INITIATED;
    } else if (id == TELEMETRY_CONTROLLER_INITIATED.toByte()) {
      return TELEMETRY_CONTROLLER_INITIATED;
    } else if (id == DISCOVERY.toByte()) {
      return DISCOVERY;
    } else {
      return new LogPageIdentifier(id);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    LogPageIdentifier that = (LogPageIdentifier) obj;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) id;
  }

}
