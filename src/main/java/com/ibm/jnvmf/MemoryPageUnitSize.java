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

package com.ibm.jnvmf;

public class MemoryPageUnitSize {

  private final Pow2Size pageSize;
  private final Pow2Size count;

  private static final Pow2Size MINIMUM_PAGE_SIZE = new Pow2Size(12);
  static final MemoryPageUnitSize MAX_VALUE = new MemoryPageUnitSize(new Pow2Size(0),
      new Pow2Size(Integer.SIZE - 2));

  public MemoryPageUnitSize(Pow2Size count) {
    this(MINIMUM_PAGE_SIZE, count);
  }

  MemoryPageUnitSize(Pow2Size pageSize, Pow2Size count) {
    this.pageSize = pageSize;
    this.count = count;
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
