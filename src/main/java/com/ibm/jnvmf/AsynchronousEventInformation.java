package com.ibm.jnvmf;

public class AsynchronousEventInformation extends EEnum<AsynchronousEventInformation.Value> {
  public class Value extends EEnum.Value {

    private final String description;

    Value(int value, String description) {
      super(value);
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  AsynchronousEventInformation() {
    super(0xff);
  }

}
