package com.ibm.jnvmf;

import java.io.IOException;

public class NvmFlushCommand extends NvmCommand<NvmFlushCommandCapsule> {
  NvmFlushCommand(IoQueuePair queuePair) throws IOException {
    super(queuePair, new NvmFlushCommandCapsule(queuePair.allocateCommandCapsule()));
  }
}
