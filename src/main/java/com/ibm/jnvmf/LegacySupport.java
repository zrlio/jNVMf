/*
 * Copyright (C) 2018, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ibm.jnvmf;

public class LegacySupport {

  // FIXME: better error handling
  static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("jnvmf.legacy", "false"));

  static void initializeSubmissionQueueEntry(NativeBuffer buffer) {
    /* Linux kernel up to 14.16 assumes SGL use in all commands */

    /*
     * set SGL type 01b - "SGLs are used for this transfer. If used, Metadata Pointer
     * (MPTR) contains an address of a single contiguous physical
     * buffer that is byte aligned."
     * although metadata is not supported by the kernel at the moment
     */
    buffer.put(1, (byte) (1 << 6));

    /* SGL Entry 1 -> keyed sgl data block descriptor */
    buffer.putLong(24, 0);
    buffer.putLong(32, 0);
    buffer.put(39, (byte) (0x4 << 4));
  }

  static void initializeControllerConfiguration(ControllerConfiguration controllerConfiguration) {
    /* Linux kernel up to 14.16 requires IOCQES and IOSQES to be set before admin queue is connected
     * i.e. before we know required IOCQES/IOSQES from identify controller command */
    if (controllerConfiguration.getIoCompletionQueueEntrySize().value() == 0) {
      controllerConfiguration.setIoCompletionQueueEntrySize(new QueueEntrySize(4));
    }
    if (controllerConfiguration.getIoSubmissionQueueEntrySize().value() == 0) {
      controllerConfiguration.setIoSubmissionQueueEntrySize(new QueueEntrySize(6));
    }
  }
}
