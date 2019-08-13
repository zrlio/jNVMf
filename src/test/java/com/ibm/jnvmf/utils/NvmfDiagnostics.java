package com.ibm.jnvmf.utils;

import com.ibm.jnvmf.AdminAsynchronousEventRequestCommand;
import com.ibm.jnvmf.AdminAsynchronousEventRequestResponseCapsule;
import com.ibm.jnvmf.AdminAsynchronousEventRequestResponseCqe;
import com.ibm.jnvmf.AdminQueuePair;
import com.ibm.jnvmf.AsynchronousEventInformation;
import com.ibm.jnvmf.AsynchronousEventType;
import com.ibm.jnvmf.Controller;
import com.ibm.jnvmf.ControllerCapabilities;
import com.ibm.jnvmf.IdentifyControllerData;
import com.ibm.jnvmf.IdentifyNamespaceData;
import com.ibm.jnvmf.LbaFormat;
import com.ibm.jnvmf.Namespace;
import com.ibm.jnvmf.Nvme;
import com.ibm.jnvmf.NvmeQualifiedName;
import com.ibm.jnvmf.NvmfTransportId;
import com.ibm.jnvmf.OperationCallback;
import com.ibm.jnvmf.RdmaException;
import com.ibm.jnvmf.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
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
    INFO,
    ASYNCEVENT
  }

  private interface TestCall {
    void call() throws IOException;
  }

  private final TestCall[] tests = new TestCall[Test.values().length];
  {
    tests[Test.INFO.ordinal()] = new PrintControllerInfo();
    tests[Test.ASYNCEVENT.ordinal()] = new GetAsyncEvent();
  }

  NvmfDiagnostics(String[] args) throws IOException, TimeoutException {

    Options options = new Options();
    Option address = Option.builder("a").required().desc("ip address").hasArg().build();
    Option port = Option.builder("p").desc("port").required().hasArg().build();
    Option subsystemNQN = Option.builder("nqn").desc("subsystem NVMe qualified name").hasArg()
        .build();
    StringBuilder testOptions = new StringBuilder();
    for (Test t  : Test.values()) {
      testOptions.append(t.name() + ", ");
    }
    Option testOption = Option.builder("t").desc(testOptions.toString()).hasArg().build();

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
      } else {
        /* default test is info */
        test = Test.INFO;
      }
    } catch (ParseException e) {
      formatter.printHelp("NvmfDiagnostics", options);
      System.exit(-1);
    }

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
        System.out.println("No namespace found");
      } else {
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
      }
      System.out.println("--------------------------------------------------------");
    }
  }

  class GetAsyncEvent implements TestCall {

    @Override
    public void call() throws IOException {
      AdminQueuePair adminQueuePair = controller.getAdminQueue();

      AdminAsynchronousEventRequestCommand asynchronousEventRequestCommand =
          new AdminAsynchronousEventRequestCommand(adminQueuePair);
      asynchronousEventRequestCommand.setCallback(new OperationCallback() {
        @Override
        public void onStart() {
          System.out.println("Sending Asynchronous Event Request...");
        }

        @Override
        public void onComplete() {
          System.out.println("Command completed waiting for event...");
        }

        @Override
        public void onFailure(RdmaException exception) {
          System.out.println(exception.getMessage());
          System.exit(-1);
        }
      });

      Response<AdminAsynchronousEventRequestResponseCapsule> asychronousEventRequestResponse =
          asynchronousEventRequestCommand.newResponse();
      asychronousEventRequestResponse.setCallback(new OperationCallback() {
        @Override
        public void onStart() { }

        @Override
        public void onComplete() {
          AdminAsynchronousEventRequestResponseCapsule response =
              asychronousEventRequestResponse.getResponseCapsule();
          AdminAsynchronousEventRequestResponseCqe asynchronousEventRequestCqe =
              response.getCompletionQueueEntry();
          System.out.println("Asynchronous Event:");
          System.out.println("\tLog Page Identifier: " +
              asynchronousEventRequestCqe.getLogPageIdentifier().toByte());
          AsynchronousEventType.Value eventType =
              asynchronousEventRequestCqe.getAsynchronousEventType();
          System.out.println("\tType: " + eventType.toInt() + " - " + eventType.getDescription());
          AsynchronousEventInformation.Value eventInformation =
              asynchronousEventRequestCqe.getAsynchronousEventInformation();
          System.out.println("\tInformation: " + eventInformation.toInt() + " - "
              + eventInformation.getDescription());
          System.out.println();

          /* TODO: Get Log Page */
          try {
            asynchronousEventRequestCommand.execute(asychronousEventRequestResponse);
          } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
          }
        }

        @Override
        public void onFailure(RdmaException exception) {
          System.out.println(exception.getMessage());
          System.exit(-1);
        }
      });
      asynchronousEventRequestCommand.execute(asychronousEventRequestResponse);
      Instant start = Instant.now();
      while (true) {
        adminQueuePair.poll();
        if (Duration.between(start, Instant.now()).toMillis() > 60000) {
          controller.keepAlive();
          start = Instant.now();
        }
      }
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
