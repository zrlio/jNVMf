package com.ibm.jnvmf;

public class AdminGetLogPageCommandSqe extends AdminSubmissionQeueueEntry {

  /*
   * NVMe Spec 1.3 - 5.14
   *
   * Dword10:
   * 0:7   Log Page Identifier
   * 08:11 Log Specific Field
   * 12:14 Reserved
   * 15    Retain Asynchronous Event
   * 16:31 Number of Dwords Lower
   *
   * Dword11:
   * 0:15 Number of Dwords Upper
   *
   * Dword12:
   * 0:31 Log Page Offset Lower
   *
   * Dword13:
   * 0:31 Log Page Offset Upper
   */

  /* Dword10/11 */
  private static final int LOG_PAGE_IDENTIFIER_OFFSET = 40;
  private static final int LOG_SPECIFIC_FIELD_OFFSET = 41;
  private static final int LOG_SPECIFIC_FIELD_BITOFFSET_START = 0;
  private static final int LOG_SPECIFIC_FIELD_BITOFFSET_END = 3;
  private static final int RETAIN_ASYNCHRONOUS_EVENT_OFFSET = 41;
  private static final int RETAIN_ASYNCHRONOUS_EVENT_BITOFFSET = 7;
  private static final int NUMBER_DWORDS_EXTENDED_OFFSET = 42;
  private static final int NUMBER_DWORDS_OFFSET = NUMBER_DWORDS_EXTENDED_OFFSET;
  private static final int NUMBER_DWORDS_MASK = (1 << 12) - 1;

  /* Dword12/13 */
  private static final int LOG_PAGE_OFFSET = 48;

  private final KeyedSglDataBlockDescriptor keyedSglDataBlockDescriptor;

  AdminGetLogPageCommandSqe(NativeBuffer buffer) {
    super(buffer);
    this.keyedSglDataBlockDescriptor = new KeyedSglDataBlockDescriptor(getSglDescriptor1Buffer());
  }

  @Override
  void initialize() {
    super.initialize();
    setOpcode(AdminCommandOpcode.GET_LOG_PAGE);
  }

  public KeyedSglDataBlockDescriptor getKeyedSglDataBlockDescriptor() {
    return keyedSglDataBlockDescriptor;
  }

  public void setNumberOfDwordsExtended(int numberOfDwords) {
    getBuffer().putInt(NUMBER_DWORDS_EXTENDED_OFFSET, numberOfDwords);
  }

  public void setNumberOfDwords(int numberOfDwords) {
    int value = numberOfDwords & NUMBER_DWORDS_MASK;
    if (value != numberOfDwords) {
      throw new IllegalArgumentException("Number of dwords to large! Only 12bits are supported");
    }
    getBuffer().putInt(NUMBER_DWORDS_OFFSET, value);
  }

  public void setRetainAsynchronousEvent(boolean retainAsynchronousEvent) {
    int raw = getBuffer().get(RETAIN_ASYNCHRONOUS_EVENT_OFFSET);
    BitUtil.setBitTo(raw, RETAIN_ASYNCHRONOUS_EVENT_BITOFFSET, retainAsynchronousEvent);
    getBuffer().put(RETAIN_ASYNCHRONOUS_EVENT_OFFSET, (byte) raw);
  }

  public void setLogPageIdentifier(LogPageIdentifier logPageIdentifier) {
    getBuffer().put(LOG_PAGE_IDENTIFIER_OFFSET, logPageIdentifier.toByte());
  }

  public void setLogPageOffset(long logPageOffset) {
    /* offset Dword aligned  -> lower 2 bits shall be cleared */
    getBuffer().putLong(LOG_PAGE_OFFSET, logPageOffset & ~0x3L);
  }
}
