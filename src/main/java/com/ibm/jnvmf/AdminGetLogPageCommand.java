package com.ibm.jnvmf;

import java.io.IOException;

public class AdminGetLogPageCommand extends AdminCommand<AdminGetLogPageCommandCapsule> {
  public AdminGetLogPageCommand(AdminQueuePair queuePair) throws IOException {
    super(queuePair, new AdminGetLogPageCommandCapsule(queuePair.allocateCommandCapsule()));
  }

  public void setLogPageSink(LogPage logPage) throws IOException {
    AdminGetLogPageCommandSqe sqe = getCommandCapsule().getSubmissionQueueEntry();
    sqe.setLogPageIdentifier(logPage.getLogPageIdentifier());
    /* we do not use the page offset for now */
    sqe.setLogPageOffset(0);
    IdentifyControllerData identifyControllerData =
        getQueuePair().getController().getIdentifyControllerData();
    if (identifyControllerData.getLogPageAttributes().hasGetLogPageExtendedDataSupport()) {
      sqe.setNumberOfDwordsExtended(logPage.getDwordSize());
    } else {
      sqe.setNumberOfDwords(logPage.getDwordSize());
    }
    KeyedSglDataBlockDescriptor keyedSglDataBlockDescriptor = sqe.getKeyedSglDataBlockDescriptor();
    keyedSglDataBlockDescriptor.set(logPage.getBuffer());
  }
}
