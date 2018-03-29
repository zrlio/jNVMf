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

public final class NamespaceIdentifier {

  static final int SIZE = 4;

  private final int namespaceIdentifier;

  /*
   * NVMe Spec 1.3 - 4.2/6.1.2
   * Applies to all namespaces attached to the controller
   */
  public static final NamespaceIdentifier ALL = new NamespaceIdentifier(0xFFFFFFFF);

  public NamespaceIdentifier(int namespaceIdentifier) {
    this.namespaceIdentifier = namespaceIdentifier;
  }

  public int toInt() {
    return namespaceIdentifier;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    NamespaceIdentifier that = (NamespaceIdentifier) obj;

    return this.namespaceIdentifier == that.namespaceIdentifier;
  }

  @Override
  public int hashCode() {
    return namespaceIdentifier;
  }

  @Override
  public String toString() {
    return Integer.toString(namespaceIdentifier);
  }
}
