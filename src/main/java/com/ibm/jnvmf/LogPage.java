package com.ibm.jnvmf;

public abstract class LogPage extends NativeData<KeyedNativeBuffer> {

  LogPage(KeyedNativeBuffer buffer, int size) {
    super(buffer, size);
  }

  abstract LogPageIdentifier getLogPageIdentifier();

  int getDwordSize() {
    return getBuffer().capacity() / 4;
  }
}
