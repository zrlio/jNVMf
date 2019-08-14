package com.ibm.jnvmf;

public class NvmFlushCommandSqe extends NvmSubmissionQueueEntry {
  NvmFlushCommandSqe(NativeBuffer buffer) {
    super(buffer);
  }

  @Override
  void initialize() {
    super.initialize();
    setOpcode(NvmCommandOpcode.FLUSH);
  }
}
