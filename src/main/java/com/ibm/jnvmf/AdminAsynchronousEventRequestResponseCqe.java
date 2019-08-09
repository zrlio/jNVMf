package com.ibm.jnvmf;

import com.ibm.jnvmf.AsynchronousEventType.Value;

public class AdminAsynchronousEventRequestResponseCqe extends AdminCompletionQueueEntry {

  /*
   * NVMe Spec 1.3a - 5.2
   *
   * Dword 0:
   * 31:24 Reserved
   * 23:16 Log Page Identifier
   * 15:08 Asychronous Event Information
   * 07:03 Reserved
   * 02:00 Asynchronous Event Type
   */
  private static final int ASYNCHRONOUS_EVENT_TYPE = 0;
  private static final int ASYNCHRONOUS_EVENT_INFORMATION_OFFSET = 1;
  private static final int LOG_PAGE_IDENTIFIER_OFFSET = 2;

  private AsynchronousEventType.Value asynchronousEventType;
  private AsynchronousEventInformation.Value asynchronousEventInformation;
  private LogPageIdentifier logPageIdentifier;



  @Override
  void update(NativeBuffer buffer) {
    super.update(buffer);
    asynchronousEventType = AsynchronousEventType.getInstance()
        .valueOf(buffer.getShort(ASYNCHRONOUS_EVENT_TYPE));
    asynchronousEventInformation =
        asynchronousEventType.valueOf(buffer.get(ASYNCHRONOUS_EVENT_INFORMATION_OFFSET));
    logPageIdentifier = LogPageIdentifier.valueOf(buffer.get(LOG_PAGE_IDENTIFIER_OFFSET));
  }

  public LogPageIdentifier getLogPageIdentifier() {
    return logPageIdentifier;
  }

  public Value getAsynchronousEventType() {
    return asynchronousEventType;
  }

  public AsynchronousEventInformation.Value getAsynchronousEventInformation() {
    return asynchronousEventInformation;
  }
}
