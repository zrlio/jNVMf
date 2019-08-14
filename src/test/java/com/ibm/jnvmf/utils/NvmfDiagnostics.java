package com.ibm.jnvmf.utils;

import com.ibm.jnvmf.AdminAsynchronousEventRequestCommand;
import com.ibm.jnvmf.AdminAsynchronousEventRequestResponseCapsule;
import com.ibm.jnvmf.AdminAsynchronousEventRequestResponseCqe;
import com.ibm.jnvmf.AdminGetLogPageCommand;
import com.ibm.jnvmf.AdminQueuePair;
import com.ibm.jnvmf.AdminResponseCapsule;
import com.ibm.jnvmf.AsynchronousEventInformation;
import com.ibm.jnvmf.AsynchronousEventType;
import com.ibm.jnvmf.ChangedNamespaceListLogPage;
import com.ibm.jnvmf.Controller;
import com.ibm.jnvmf.ControllerCapabilities;
import com.ibm.jnvmf.ErrorInformationLogEntry;
import com.ibm.jnvmf.ErrorInformationLogPage;
import com.ibm.jnvmf.IdentifyControllerData;
import com.ibm.jnvmf.IdentifyNamespaceData;
import com.ibm.jnvmf.KeyedNativeBuffer;
import com.ibm.jnvmf.LbaFormat;
import com.ibm.jnvmf.LogPage;
import com.ibm.jnvmf.LogPageIdentifier;
import com.ibm.jnvmf.Namespace;
import com.ibm.jnvmf.NamespaceIdentifier;
import com.ibm.jnvmf.Nvme;
import com.ibm.jnvmf.NvmeQualifiedName;
import com.ibm.jnvmf.NvmfTransportId;
import com.ibm.jnvmf.OperationCallback;
import com.ibm.jnvmf.RdmaException;
import com.ibm.jnvmf.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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

  private NvmfDiagnostics(String[] args) throws IOException, TimeoutException {

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

  private void connect(NvmfTransportId tid) throws IOException, TimeoutException {
    nvme = new Nvme();
    controller = nvme.connect(tid);
    controller.getControllerConfiguration().setEnable(true);
    controller.syncConfiguration();
    controller.waitUntilReady();
  }

  private void run() throws IOException {
    tests[test.ordinal()].call();
  }

  private boolean stopPoll = false;

  private void getLogPage(LogPageIdentifier logPageIdentifier) throws IOException {
    AdminQueuePair adminQueuePair = controller.getAdminQueue();
    /* TODO: make configurable */
    ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
    KeyedNativeBuffer logPageBuffer = adminQueuePair.registerMemory(buffer);



    AdminGetLogPageCommand adminGetLogPageCommand =
        new AdminGetLogPageCommand(adminQueuePair);
    adminGetLogPageCommand.setCallback(new OperationCallback() {
      @Override
      public void onStart() {
        System.out.println("Sending Get Loge Page command...");
      }

      @Override
      public void onComplete() { }

      @Override
      public void onFailure(RdmaException exception) {
        System.out.println(exception.getMessage());
        stopPoll = true;
      }
    });
    Response<AdminResponseCapsule> adminGetLogPageResponse =
        adminGetLogPageCommand.newResponse();

    LogPage logPage = null;
    if (logPageIdentifier.equals(LogPageIdentifier.ERROR_INFORMATION)) {
      logPage = new ErrorInformationLogPage(logPageBuffer,
          logPageBuffer.capacity() / ErrorInformationLogEntry.SIZE);
    } else if (logPageIdentifier.equals(LogPageIdentifier.CHANGED_NAMESPACE_LIST)) {
      logPage = new ChangedNamespaceListLogPage(logPageBuffer);
    } else {
      System.out.println("Log page type not supported yet");
      return;
    }

    final LogPage finalLogPage = logPage;
    adminGetLogPageResponse.setCallback(new OperationCallback() {
      @Override
      public void onStart() {

      }

      @Override
      public void onComplete() {
        System.out.println("Get Log Page Command completed...");
        if (logPageIdentifier.equals(LogPageIdentifier.ERROR_INFORMATION)) {
          System.out.println("Error Information Log Page:");
          ErrorInformationLogPage errorInformationLogPage =
              (ErrorInformationLogPage) finalLogPage;
          for (int i = 0; i < errorInformationLogPage.getNumLogEntries(); i++) {
            ErrorInformationLogEntry logEntry = errorInformationLogPage.getLogEntry(i);
            if (logEntry.getErrorCount().isValid()) {
              System.out.println("Log Entry " + logEntry.getErrorCount().getValue());
              System.out.println("\tSubmission Queue ID = " +
                  logEntry.getSubmissionQueueId().toString());
              System.out.println("\tCommand ID = " + logEntry.getCommandId());
              /* TODO: all of status field */
              System.out.println("\tStatus Code Type = " +
                  logEntry.getStatusField().getStatusCodeType().toInt() + " " +
                  logEntry.getStatusField().getStatusCodeType().getDescription());
              System.out.println("\tParameter Error Location = {bit = " +
                  logEntry.getParameterErrorLocation().getBitLocation() +
                  ", byte = " + logEntry.getParameterErrorLocation().getByteLocation() + "}");
              System.out.println("\tLBA = " + logEntry.getLba());
              System.out.println("\tNamespace = " + logEntry.getNamespaceIdentifier());
              System.out.println("\tVendor Specific Information Available = " +
                  logEntry.isVendorSpecificInformationAvailable());
              if (logEntry.isVendorSpecificInformationAvailable()) {
                System.out.println("\tVendor Specific Information Log Page Id = " +
                    logEntry.getVendorSpecificInformationLogPage().toByte());
              }
              System.out.println("\tCommand Specific Information = " +
                  logEntry.getCommandSpecificInformation());
            }
          }
        } else if (logPageIdentifier.equals(LogPageIdentifier.CHANGED_NAMESPACE_LIST)) {
          System.out.println("Changed Namespace List Log Page:");
          ChangedNamespaceListLogPage changedNamespaceListLogPage =
              (ChangedNamespaceListLogPage)finalLogPage;
          for (int i = 0; i < ChangedNamespaceListLogPage.getNumberOfEntries(); i++) {
            NamespaceIdentifier namespaceIdentifier =
                changedNamespaceListLogPage.getNamespaceIdentifier(i);
            if (namespaceIdentifier != null) {
              System.out.println("\t" + i + " = " + namespaceIdentifier);
            }
          }
        } else {
          System.out.println("Log page type not supported yet ???");
        }
        stopPoll = true;
      }

      @Override
      public void onFailure(RdmaException exception) {
        System.out.println(exception.getMessage());
        stopPoll = true;
      }
    });

    adminGetLogPageCommand.execute(adminGetLogPageResponse);

    while (!stopPoll) {
      adminQueuePair.poll();
    }
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
          System.out.println("Asynchronous Event Request command completed waiting for event...");
        }

        @Override
        public void onFailure(RdmaException exception) {
          System.out.println(exception.getMessage());
          stopPoll = true;
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
          LogPageIdentifier logPageIdentifier = asynchronousEventRequestCqe.getLogPageIdentifier();
          System.out.println("\tLog Page Identifier: " + logPageIdentifier.toByte());
          AsynchronousEventType.Value eventType =
              asynchronousEventRequestCqe.getAsynchronousEventType();
          System.out.println("\tType: " + eventType.toInt() + " - " + eventType.getDescription());
          AsynchronousEventInformation.Value eventInformation =
              asynchronousEventRequestCqe.getAsynchronousEventInformation();
          System.out.println("\tInformation: " + eventInformation.toInt() + " - "
              + eventInformation.getDescription());
          System.out.println();

          try {
            getLogPage(logPageIdentifier);
            stopPoll = false;
            asynchronousEventRequestCommand.execute(asychronousEventRequestResponse);
          } catch (IOException e) {
            e.printStackTrace();
            stopPoll = true;
          }
        }

        @Override
        public void onFailure(RdmaException exception) {
          System.out.println(exception.getMessage());
          stopPoll = true;
        }
      });
      asynchronousEventRequestCommand.execute(asychronousEventRequestResponse);
      Instant start = Instant.now();
      while (!stopPoll) {
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
