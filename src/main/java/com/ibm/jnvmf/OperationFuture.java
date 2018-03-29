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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class OperationFuture<O extends Operation, T> implements OperationCallback,
    Future<T> {

  private final QueuePair queuePair;
  private RdmaException rdmaException;
  private final O operation;
  private volatile boolean done;

  OperationFuture(QueuePair queuePair, O operation) {
    this.done = false;
    this.queuePair = queuePair;
    this.operation = operation;
    operation.setCallback(this);
  }

  private final void checkCompleted() {
    if (isDone()) {
      throw new IllegalStateException("Operation already completed");
    }
  }

  @Override
  public void onStart() {
    /* for now we don't allow reusing futures */
  }

  @Override
  public void onComplete() {
    checkCompleted();
    this.done = true;
  }

  @Override
  public void onFailure(RdmaException exception) {
    checkCompleted();
    this.rdmaException = exception;
    this.done = true;
  }

  abstract T getT();

  O getOperation() {
    return operation;
  }

  @Override
  public boolean cancel(boolean cancel) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  private final void checkStatus() throws ExecutionException {
    if (rdmaException != null) {
      throw new ExecutionException(rdmaException);
    }
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    while (!done) {
      try {
        queuePair.poll();
      } catch (IOException exception) {
        throw new ExecutionException(exception);
      }
    }
    checkStatus();
    return getT();
  }

  @Override
  public T get(long timeout, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (!done) {
      long start = System.nanoTime();
      long end = start + TimeUnit.NANOSECONDS.convert(timeout, timeUnit);
      boolean waitTimeOut;
      do {
        try {
          queuePair.poll();
        } catch (IOException exception) {
          throw new ExecutionException(exception);
        }
        waitTimeOut = System.nanoTime() > end;
      } while (!done && !waitTimeOut);
      if (!done && waitTimeOut) {
        throw new TimeoutException("get wait time out!");
      }
    }
    checkStatus();
    return getT();
  }
}
