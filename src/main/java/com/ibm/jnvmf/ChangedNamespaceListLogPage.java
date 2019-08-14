package com.ibm.jnvmf;

public class ChangedNamespaceListLogPage extends LogPage {

  public static final int SIZE = NamespaceIdentifierList.SIZE;

  private final NamespaceIdentifierList namespaceIdentifierList;

  public ChangedNamespaceListLogPage(KeyedNativeBuffer buffer) {
    super(buffer, SIZE);
    this.namespaceIdentifierList = new NamespaceIdentifierList(buffer);
  }

  @Override
  LogPageIdentifier getLogPageIdentifier() {
    return LogPageIdentifier.CHANGED_NAMESPACE_LIST;
  }

  public NamespaceIdentifier getNamespaceIdentifier(int index) {
    return namespaceIdentifierList.getIdentifier(index);
  }

  public static int getNumberOfEntries() {
    return SIZE / NamespaceIdentifier.SIZE;
  }

  @Override
  void initialize() { }
}
