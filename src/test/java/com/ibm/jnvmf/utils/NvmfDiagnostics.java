package com.ibm.jnvmf.utils;

import com.ibm.jnvmf.Controller;
import com.ibm.jnvmf.ControllerCapabilities;
import com.ibm.jnvmf.IdentifyControllerData;
import com.ibm.jnvmf.IdentifyNamespaceData;
import com.ibm.jnvmf.LbaFormat;
import com.ibm.jnvmf.Namespace;
import com.ibm.jnvmf.Nvme;
import com.ibm.jnvmf.NvmeQualifiedName;
import com.ibm.jnvmf.NvmfTransportId;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class NvmfDiagnostics {

  private Controller controller;
  private Test test;
  private Nvme nvme;

  private enum Test {
    INFO
  }

  private interface TestCall {
    void call() throws IOException;
  }

  private final TestCall[] tests = new TestCall[Test.values().length];
  {
    tests[Test.INFO.ordinal()] = new PrintControllerInfo();
  }

  NvmfDiagnostics(String[] args) throws IOException, TimeoutException {

    Options options = new Options();
    Option address = Option.builder("a").required().desc("ip address").hasArg().build();
    Option port = Option.builder("p").desc("port").required().hasArg().build();
    Option subsystemNQN = Option.builder("nqn").desc("subsystem NVMe qualified name").hasArg()
        .build();
    Option testOption = Option.builder("t").desc("info|test").hasArg().build();

    options.addOption(address);
    options.addOption(port);
    options.addOption(subsystemNQN);
    options.addOption(testOption);

    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    HelpFormatter formatter = new HelpFormatter();
    try {
      line = parser.parse(options, args);
      if (line.hasOption(testOption.getOpt())) {
        for (Test t : Test.values()) {
          if (t.name().equalsIgnoreCase(line.getOptionValue(testOption.getOpt()))) {
            test = t;
          }
        }
        if (test == null) {
          throw new ParseException("No test option with name " + testOption.getOpt());
        }
      }
    } catch (ParseException e) {
      formatter.printHelp("NvmfDiagnostics", options);
      System.exit(-1);
    }
    /* default test is info */
    test = Test.INFO;

    NvmfTransportId transportId;
    InetSocketAddress socketAddress = new InetSocketAddress(line.getOptionValue(address.getOpt()),
        Integer.parseInt(line.getOptionValue(port.getOpt())));
    transportId = new NvmfTransportId(socketAddress,
        new NvmeQualifiedName(line.getOptionValue(subsystemNQN.getOpt())));

    connect(transportId);
  }

  void connect(NvmfTransportId tid) throws IOException, TimeoutException {
    nvme = new Nvme();
    controller = nvme.connect(tid);
    controller.getControllerConfiguration().setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
  }

  void run() throws IOException {
    tests[test.ordinal()].call();
  }

  private class PrintControllerInfo implements TestCall {

    @Override
    public void call() throws IOException {
      IdentifyControllerData identifyControllerData = controller.getIdentifyControllerData();
      System.out.println("Identify Controller Data: \n" +
          "PCI Vendor ID: " + identifyControllerData.getPciVendorId() + "\n" +
          "PCI Subsystem Vendor ID: " + identifyControllerData.getPciSubsystemVendorId() + "\n" +
          "Serial Number: " + identifyControllerData.getSerialNumber() + "\n" +
          "Model Number: " + identifyControllerData.getModelNumber() + "\n" +
          "Firmware Revision: " + identifyControllerData.getFirmwareRevision() + "\n" +
          "Maximum Data Transfer Size: " + identifyControllerData.getMaximumDataTransferSize() + "\n"
          +
          "Controller ID: " + identifyControllerData.getControllerId().toShort() + "\n" +
          "NVMe Version: " + identifyControllerData.getNvmeVersion() + "\n" +
          "Required Submission Queue Entry Size: " + identifyControllerData
          .getRequiredSubmissionQueueEntrySize() + "\n" +
          "Maximum Submission Queue Entry Size: " + identifyControllerData
          .getMaximumSubmissionQueueEntrySize() + "\n" +
          "Required Completion Queue Entry Size: " + identifyControllerData
          .getRequiredCompletionQueueEntrySize() + "\n" +
          "Maximum Completion Queue Entry Size: " + identifyControllerData
          .getMaximumCompletionQueueEntrySize() + "\n" +
          "IO Queue Command Capsule Size: " + identifyControllerData
          .getIoQueueCommandCapsuleSupportedSize() + "\n" +
          "IO Queue Response Capsule Size: " + identifyControllerData
          .getIoQueueResponseCapsuleSupportedSize() + "\n" +
          "In Capsule Data Offset: " + identifyControllerData.getInCapsuleDataOffset() + "\n" +
          "Maximum SGL Data Block Descriptors: " + identifyControllerData
          .getMaximumSglDataBlockDescriptors());
      System.out.println("--------------------------------------------------------");
      ControllerCapabilities controllerCapabilities = controller.getControllerCapabilities();
      System.out.println("Controller Capabilities: \n" +
          "Maximum Queue Entries Supported: " + controllerCapabilities
          .getMaximumQueueEntriesSupported() + "\n" +
          "Contiguous Queues Required: " + controllerCapabilities.getContiguousQueuesRequired() + "\n"
          +
          "Arbitration Mechanism Supported: ");
      controllerCapabilities.getArbitrationMechanismSupported()
          .forEach(value -> System.out.print(value.getDescription() + ", "));
      System.out.println("\n" +
          "Timeout: " + controllerCapabilities.getTimeout() + " " + ControllerCapabilities
          .getTimeoutUnit().toString().toLowerCase() + "\n" +
          "NVM Subsystem Reset Supported: " + controllerCapabilities.getNvmSubsystemResetSupported()
          + "\n" +
          "Memory Page Size Minimum: " + controllerCapabilities.getMemoryPageSizeMinimum() + "\n" +
          "Memory Page Size Maximum: " + controllerCapabilities.getMemoryPageSizeMaximum());
      System.out.println("--------------------------------------------------------");
      List<Namespace> namespaces = controller.getActiveNamespaces();
      if (namespaces.isEmpty()) {
        throw new IllegalStateException("No namespace found");
      }
      System.out.println("Namespaces:");
      for (Namespace namespace : namespaces) {
        System.out.println("Id: " + namespace.getIdentifier().toInt());
        IdentifyNamespaceData namespaceData = namespace.getIdentifyNamespaceData();
        System.out.println("Size: " + namespaceData.getNamespaceSize());
        System.out.println("Capacity: " + namespaceData.getNamespaceCapacity());
        LbaFormat lbaFormat = namespaceData.getFormattedLbaSize();
        System.out.println("Formatted LBA: " +
            "\n\tData Size: " + lbaFormat.getLbaDataSize() +
            "\n\tMetadata Size: " + lbaFormat.getMetadataSize() +
            "\n\tRelative Performance: " + lbaFormat.getRelativePerformance().toInt());
      }
      System.out.println("--------------------------------------------------------");
    }
  }

  void close() throws IOException {
    controller.free();
  }

  public static void main(String[] args) throws IOException, TimeoutException {
    NvmfDiagnostics diagnostics = new NvmfDiagnostics(args);
    diagnostics.run();
    diagnostics.close();
  }
}
