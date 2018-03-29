package com.ibm.jnvmf;

public class MemoryPageUnitSize {

  private final Pow2Size pageSize;
  private final Pow2Size count;

  static final Pow2Size MINIMUM_PAGE_SIZE = new Pow2Size(12);

  public MemoryPageUnitSize(Pow2Size count) {
    this.pageSize = MINIMUM_PAGE_SIZE;
    this.count = count;
  }

  MemoryPageUnitSize(Pow2Size pageSize, Pow2Size count) {
    this.pageSize = pageSize;
    this.count = count;
  }

  public Pow2Size getPageSize() {
    return pageSize;
  }

  Pow2Size value() {
    return count;
  }

  public Pow2Size toPow2Size() {
    return new Pow2Size(count.value() + pageSize.value());
  }

  public int toInt() {
    return pageSize.toInt() * count.toInt();
  }

  @Override
  public String toString() {
    return Integer.toString(toInt());
  }
}
