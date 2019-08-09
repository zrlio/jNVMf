package com.ibm.jnvmf;

public class AdminAsynchronousEventRequestResponseCapsule extends
    ResponseCapsule<AdminAsynchronousEventRequestResponseCqe> {

  public AdminAsynchronousEventRequestResponseCapsule() {
    super(new AdminAsynchronousEventRequestResponseCqe());
  }
}
