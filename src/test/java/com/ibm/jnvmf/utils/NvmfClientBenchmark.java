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

package com.ibm.jnvmf.utils;

import com.ibm.jnvmf.Command;
import com.ibm.jnvmf.Controller;
import com.ibm.jnvmf.ControllerCapabilities;
import com.ibm.jnvmf.GenericStatusCode;
import com.ibm.jnvmf.IoQueuePair;
import com.ibm.jnvmf.IdentifyControllerData;
import com.ibm.jnvmf.IdentifyNamespaceData;
import com.ibm.jnvmf.KeyedNativeBuffer;
import com.ibm.jnvmf.LbaFormat;
import com.ibm.jnvmf.Namespace;
import com.ibm.jnvmf.NamespaceIdentifier;
import com.ibm.jnvmf.NativeBuffer;
import com.ibm.jnvmf.NvmCompletionQueueEntry;
import com.ibm.jnvmf.NvmIoCommand;
import com.ibm.jnvmf.NvmIoCommandCapsule;
import com.ibm.jnvmf.NvmIoCommandSqe;
import com.ibm.jnvmf.NvmReadCommand;
import com.ibm.jnvmf.NvmResponseCapsule;
import com.ibm.jnvmf.NvmWriteCommand;
import com.ibm.jnvmf.Nvme;
import com.ibm.jnvmf.NvmeQualifiedName;
import com.ibm.jnvmf.NvmfTransportId;
import com.ibm.jnvmf.OperationCallback;
import com.ibm.jnvmf.RdmaException;
import com.ibm.jnvmf.Response;
import com.ibm.jnvmf.StatusCode;
import com.ibm.jnvmf.SubmissionQueueEntry;
import com.ibm.jnvmf.UnsuccessfulComandException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class NvmfClientBenchmark {

  private Controller controller;
  private IoQueuePair queuePair;
  private Nvme nvme;

  private final List<Long> stats;
  private int interval;
  private long runs;
  private int queueDepth;
  private int transferSize;
  private AccessPattern accessPattern;
  private boolean write;
  private final boolean doStats;
  private int align;
  private final boolean inline;
  private int queueSize;
  private final boolean incapsuleData;
  private PrintWriter logWriter;

  private final ThreadLocalRandom random;

  enum AccessPattern {
    SEQUENTIAL,
    RANDOM,
    SAME
  }

  NvmfClientBenchmark(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    this.stats = new ArrayList<>();
    this.random = ThreadLocalRandom.current();

    Options options = new Options();
    Option address = Option.builder("a").required().desc("ip address").hasArg().build();
    Option port = Option.builder("p").desc("port").required().hasArg().build();
    Option subsystemNQN = Option.builder("nqn").desc("subsystem NVMe qualified name").hasArg()
        .build();
    Option intervalOption = Option.builder("i").required().desc("interval (seconds)").hasArg()
        .type(Number.class).build();
    Option runsOption = Option.builder("n").required().desc("number of runs").hasArg()
        .type(Number.class).build();
    Option queueDepthOption = Option.builder("qd").required().desc("queue depth").hasArg()
        .type(Number.class).build();
    Option queueSizeOption = Option.builder("qs").desc("submission queue size").hasArg()
        .type(Number.class).build();
    Option sizeOption = Option.builder("s").required().desc("size (bytes)").hasArg()
        .type(Number.class).build();
    Option accessPatternOption = Option.builder("m").required()
        .desc("access pattern: SEQUENTIAL/RANDOM/SAME").hasArg().build();
    Option readWrite = Option.builder("rw").required().desc("read/write").hasArg().build();
    Option alignOption = Option.builder("g").desc("align to (default 1)").hasArg()
        .type(Number.class).build();
    Option histogram = Option.builder("H").desc("print historgram").build();
    Option inlineOption = Option.builder("I").desc("use RDMA inline data").build();
    Option incapsuleOption = Option.builder("ic").desc("use incapsule data").build();
    Option logOption = Option.builder("o").desc("log results to file").hasArg().build();

    options.addOption(address);
    options.addOption(port);
    options.addOption(subsystemNQN);
    options.addOption(runsOption);
    options.addOption(intervalOption);
    options.addOption(queueDepthOption);
    options.addOption(queueSizeOption);
    options.addOption(sizeOption);
    options.addOption(accessPatternOption);
    options.addOption(readWrite);
    options.addOption(alignOption);
    options.addOption(histogram);
    options.addOption(inlineOption);
    options.addOption(incapsuleOption);
    options.addOption(logOption);

    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    HelpFormatter formatter = new HelpFormatter();
    runs = 0;
    transferSize = 0;
    interval = 0;
    align = 1;
    queueSize = 128;
    try {
      line = parser.parse(options, args);
      runs = ((Number) line.getParsedOptionValue(runsOption.getOpt())).intValue();
      queueDepth = ((Number) line.getParsedOptionValue(queueDepthOption.getOpt())).intValue();
      transferSize = ((Number) line.getParsedOptionValue(sizeOption.getOpt())).intValue();
      interval = ((Number) line.getParsedOptionValue(intervalOption.getOpt())).intValue();
      if (line.hasOption(alignOption.getOpt())) {
        align = ((Number) line.getParsedOptionValue(alignOption.getOpt())).intValue();
      }
      if (line.hasOption(queueSizeOption.getOpt())) {
        queueSize = ((Number) line.getParsedOptionValue(queueSizeOption.getOpt())).intValue();
      }
    } catch (ParseException e) {
      formatter.printHelp("nvmf", options);
      System.exit(-1);
    }

    NvmfTransportId transportId;
    InetSocketAddress socketAddress = new InetSocketAddress(line.getOptionValue(address.getOpt()),
        Integer.parseInt(line.getOptionValue(port.getOpt())));
    transportId = new NvmfTransportId(socketAddress,
        new NvmeQualifiedName(line.getOptionValue(subsystemNQN.getOpt())));

    accessPattern = AccessPattern.valueOf(line.getOptionValue(accessPatternOption.getOpt()));
    String str = line.getOptionValue("rw");
    if (str.compareTo("write") == 0) {
      write = true;
    } else if (str.compareTo("read") == 0) {
      write = false;
    } else {
      throw new IllegalArgumentException("rw can only be \"read\" or \"write\"");
    }
    doStats = line.hasOption(histogram.getOpt());
    inline = line.hasOption(inlineOption.getOpt());
    incapsuleData = line.hasOption(incapsuleOption.getOpt());
    if (!write && incapsuleData) {
      throw new IllegalArgumentException("read does not support incapsule data");
    }

    if (line.hasOption(logOption.getOpt())) {
      String path = line.getOptionValue(logOption.getOpt());
      logWriter = new PrintWriter(new FileOutputStream(new File(path), true));
    }

    System.out
        .println((write ? "write" : "read") + " " + transferSize + "bytes with QD = " + queueDepth +
            ", time[s] = " + interval + ", pattern = " + accessPattern.name() + ", runs = " + runs);
    connect(transportId);
  }

  void close() throws IOException {
    if (logWriter != null) {
      logWriter.flush();
      logWriter.close();
    }
    controller.free();
  }

  void connect(NvmfTransportId tid) throws IOException, TimeoutException {
    nvme = new Nvme();
    controller = nvme.connect(tid);
    controller.getControllerConfiguration().setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();

    IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
    System.out.println("Identify Controller Data: \n" +
        "PCI Vendor ID: " + identifyControllerData.getPciVendorId() + "\n" +
        "PCI Subsystem Vendor ID: " + identifyControllerData.getPciSubsystemVendorId() + "\n" +
        "Serial Number: " + identifyControllerData.getSerialNumber() + "\n" +
        "Model Number: " + identifyControllerData.getModelNumber() + "\n" +
        "Controller ID: " + identifyControllerData.getControllerId().toShort());
    System.out.println("--------------------------------------------------------");
    int inlineDataSize = 0;
    if (inline) {
      inlineDataSize = SubmissionQueueEntry.SIZE;
      if (incapsuleData) {
        inlineDataSize += transferSize;
      }
    }
    int incapsuleDataSize = 0;
    if (incapsuleData) {
      incapsuleDataSize = transferSize;
    }
    queuePair = controller.createIoQueuePair(queueSize, 0, incapsuleDataSize, inlineDataSize);
  }

  void printRunStats(long operations, long actualTime) throws IOException {
    long iops = (operations * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)) / actualTime;
    long meanNs;
    if (doStats) {
      Collections.sort(stats);
      long sum = 0;
      for (long x : stats) {
        sum += x;
      }
      meanNs = sum / operations;
    } else {
      meanNs = actualTime / operations;
    }

    if (logWriter != null) {
      IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
      List<Namespace> namespaces = controller.getActiveNamespaces();
      DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
      Date date = new Date();
      logWriter.print(dateFormat.format(date));
      logWriter.print(",");
      logWriter.print(identifyControllerData.getModelNumber());
      logWriter.print(",");
      IdentifyNamespaceData identifyNamespaceData = namespaces.get(0).getIdentifyNamespaceData();
      logWriter.print(identifyNamespaceData.getNamespaceCapacity());
      logWriter.print(",");
      LbaFormat lbaFormat = identifyNamespaceData.getFormattedLbaSize();
      logWriter.print(lbaFormat.getLbaDataSize());
      logWriter.print(",");
      logWriter.print(lbaFormat.getMetadataSize());
      logWriter.print(",");
      logWriter.print(transferSize);
      logWriter.print(",");
      logWriter.print(queueDepth);
      logWriter.print(",");
      if (accessPattern == AccessPattern.RANDOM) {
        logWriter.print("rand");
      }
      if (write) {
        logWriter.print("write");
      } else {
        logWriter.print("read");
      }
      logWriter.print(",");
      logWriter.print(interval);
      logWriter.print(",");
      logWriter.print("jNVMf");
      logWriter.print(",,");
    }

    System.out.print("t[ns] " + actualTime +
        ", #ops " + operations +
        ", iops " + iops +
        ", mean[ns] " + meanNs);
    double tpMBs = iops * transferSize / (1024.0 * 1024.0);
    System.out.printf(", tp[MB/s] %.2f", tpMBs);
    if (doStats) {
      long min = stats.get(0);
      long max = stats.get(stats.size() - 1);
      System.out.print(", min[ns] " + min +
          ", max[ns] " + max);
      double percentiles[] = new double[]{
          0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999, 0.9999
      };
      for (double percentile : percentiles) {
        System.out.print(
            ", p" + (percentile * 100.0) + "% " + stats.get((int) (stats.size() * percentile)));
      }
      if (logWriter != null) {
        logWriter.print(stats.get((int) (stats.size() * 0.5)) / 1000.0);
        logWriter.print(",");
        logWriter.print(stats.get((int) (stats.size() * 0.9)) / 1000.0);
        logWriter.print(",");
        logWriter.print(stats.get((int) (stats.size() * 0.99)) / 1000.0);
        logWriter.print(",");
        logWriter.print(stats.get((int) (stats.size() * 0.999)) / 1000.0);
        logWriter.print(",");
      }
    } else if (logWriter != null) {
      logWriter.print(",,,,");
    }
    if (logWriter != null) {
      logWriter.print(iops);
      logWriter.print(",");
      logWriter.print(tpMBs);
      logWriter.print(",");
      logWriter.print(",");
      if (inline) {
        logWriter.print("inline");
      }
      if (incapsuleData) {
        logWriter.print("/incapsuleData");
      }
      logWriter.println();
    }
    System.out.println();
  }

  void run() throws IOException, ExecutionException, InterruptedException, RdmaException {
    KeyedNativeBuffer buffers[] = null;
    if (!incapsuleData) {
      buffers = new KeyedNativeBuffer[queueDepth];
      for (int i = 0; i < queueDepth; i++) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(transferSize);
        KeyedNativeBuffer registeredBuffer = queuePair.registerMemory(buffer);
        byte bytes[] = new byte[registeredBuffer.capacity()];
        random.nextBytes(bytes);
        registeredBuffer.put(bytes);
        registeredBuffer.clear();
        buffers[i] = registeredBuffer;
      }
    }

    final Queue<RdmaException> rdmaException = new ArrayDeque<>(queueDepth);
    final Queue<Command> commands = new ArrayDeque<>(queueDepth);
    final Queue<Response> responses = new ArrayDeque<>(queueDepth);
    for (int i = 0; i < queueDepth; i++) {
      NvmIoCommand<? extends NvmIoCommandCapsule> command;
      if (write) {
        NvmWriteCommand writeCommand = new NvmWriteCommand(queuePair);
        if (incapsuleData) {
          NativeBuffer buffer = writeCommand.getIncapsuleData();
          byte bytes[] = new byte[transferSize];
          random.nextBytes(bytes);
          buffer.put(bytes);
          buffer.flip();
          writeCommand.setIncapsuleData(buffer);
        }
        command = writeCommand;
      } else {
        command = new NvmReadCommand(queuePair);
      }
      command.setSendInline(inline);
      NvmIoCommandCapsule commandCapsule = command.getCommandCapsule();
      if (!incapsuleData) {
        commandCapsule.setSglDescriptor(buffers[i]);
      }
      command.setCallback(new OperationCallback() {
        @Override
        public void onStart() {
        }

        @Override
        public void onComplete() {
          commands.add(command);
        }

        @Override
        public void onFailure(RdmaException e) {
          rdmaException.add(e);
        }
      });
      commands.add(command);
      Response<NvmResponseCapsule> response = command.newResponse();
      response.setCallback(new OperationCallback() {
        long startTime;

        @Override
        public void onStart() {
          if (doStats) {
            startTime = System.nanoTime();
          }
        }

        @Override
        public void onComplete() {
          if (doStats) {
            stats.add(System.nanoTime() - startTime);
          }
          responses.add(response);
        }

        @Override
        public void onFailure(RdmaException e) {
          rdmaException.add(e);
        }
      });
      responses.add(response);
    }

    final Namespace namespace = controller.getActiveNamespaces().get(0);
    LbaFormat lbaFormat = namespace.getIdentifyNamespaceData().getFormattedLbaSize();
    final int sectorSize = lbaFormat.getLbaDataSize().toInt();
    final int sectorCount = transferSize / sectorSize;
    final long totalSizeSector = namespace.getIdentifyNamespaceData().getNamespaceCapacity();

    NamespaceIdentifier namespaceIdentifier = new NamespaceIdentifier(0x1);

    long intervalNs = TimeUnit.NANOSECONDS.convert(interval, TimeUnit.SECONDS);
    for (int r = 0; r < runs; r++) {
      // start at random offset
      long lba = random.nextLong(totalSizeSector - sectorCount);
      lba -= lba % align;

      long actualTime;
      long operations = 0;
      stats.clear();
      long nextTime = System.nanoTime() + intervalNs;
      outer:
      while (true) {
        for (int i = 0; i < Math.min(commands.size(), responses.size()); i++) {
          actualTime = System.nanoTime();
          if (actualTime > nextTime) {
            break outer;
          }
          Response<NvmResponseCapsule> response = responses.remove();
          NvmCompletionQueueEntry cqe = response.getResponseCapsule().getCompletionQueueEntry();
          StatusCode.Value statusCode = cqe.getStatusCode();
          if (statusCode != null) {
            if (!statusCode.equals(GenericStatusCode.getInstance().SUCCESS)) {
              throw new UnsuccessfulComandException(cqe);
            }
          }

          NvmIoCommand<?> command = (NvmIoCommand) commands.remove();
          NvmIoCommandCapsule commandCapsule = command.getCommandCapsule();
          NvmIoCommandSqe sqe = commandCapsule.getSubmissionQueueEntry();
          sqe.setStartingLba(lba);
          sqe.setNumberOfLogicalBlocks(sectorCount);
          sqe.setNamespaceIdentifier(namespaceIdentifier);
          command.execute(response);
          switch (accessPattern) {
            case SEQUENTIAL:
              lba += sectorCount;
              lba = lba % (totalSizeSector - sectorCount);
              break;
            case RANDOM:
              lba = random.nextLong(totalSizeSector - sectorCount);
              break;
          }
          lba -= lba % align;
          operations++;
        }
        do {
          queuePair.poll();
        } while ((commands.isEmpty() || responses.isEmpty()) && !rdmaException.isEmpty());
        if (!rdmaException.isEmpty()) {
          throw rdmaException.remove();
        }
      }
      printRunStats(operations, actualTime - nextTime + intervalNs);
    }

    if (!incapsuleData) {
      for (KeyedNativeBuffer buffer : buffers) {
        buffer.free();
      }
    }
    for (Command command : commands) {
      command.getCommandCapsule().free();
    }
  }

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, IOException, TimeoutException, RdmaException {
    NvmfClientBenchmark benchmark = new NvmfClientBenchmark(args);
    benchmark.run();
    benchmark.close();
  }
}
