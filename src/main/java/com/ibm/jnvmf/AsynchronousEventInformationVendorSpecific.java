package com.ibm.jnvmf;

public class AsynchronousEventInformationVendorSpecific extends AsynchronousEventInformation {
  private static final AsynchronousEventInformationVendorSpecific instance =
      new AsynchronousEventInformationVendorSpecific();

  public static AsynchronousEventInformationVendorSpecific getInstance() {
    return instance;
  }
}
