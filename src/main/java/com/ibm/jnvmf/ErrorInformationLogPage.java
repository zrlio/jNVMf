package com.ibm.jnvmf;

public class ErrorInformationLogPage extends LogPage {

  /*
   * NVMe Spec 1.3a - 5.14.1.1
   */

  public ErrorInformationLogPage(KeyedNativeBuffer buffer, int entryCount) {
    super(buffer, entryCount * ErrorInformationLogEntry.SIZE);
  }

  @Override
  LogPageIdentifier getLogPageIdentifier() {
    return LogPageIdentifier.ERROR_INFORMATION;
  }

  public ErrorInformationLogEntry getLogEntry(int index) {
    int offset = index * ErrorInformationLogEntry.SIZE;
    if (offset >= getBuffer().capacity() || index < 0) {
      throw new IllegalArgumentException("Log entry index out of bound (min = 0, max = "
          + (getNumLogEntries() - 1) + ")");
    }
    getBuffer().clear();
    getBuffer().position(offset);
    return new ErrorInformationLogEntry(getBuffer());
  }

  public int getNumLogEntries() {
    return getBuffer().capacity() / ErrorInformationLogEntry.SIZE;
  }

  @Override
  void initialize() { }
}
