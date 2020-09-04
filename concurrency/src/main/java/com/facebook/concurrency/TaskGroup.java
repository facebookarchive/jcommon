/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.concurrency;

import com.facebook.collections.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides a grouping of tasks so that when executed, the caller will have a latch to wait on to
 * indicate when all registered tasks have completed. Tasks may be registered with the default
 * executor, or with another executor.
 */
public class TaskGroup {
  private final ExecutorService defaultExecutor;
  private final Collection<Pair<ExecutorService, Runnable>> taskPairs = new ArrayList<>();

  public TaskGroup(ExecutorService defaultExecutor) {
    this.defaultExecutor = defaultExecutor;
  }

  public TaskGroup() {
    this(null);
  }

  public synchronized void register(ExecutorService executorService, Runnable task) {
    taskPairs.add(new Pair<>(executorService, task));
  }

  public void register(Runnable task) {
    if (defaultExecutor == null) {
      throw new IllegalStateException("No default executor specified");
    }
    register(defaultExecutor, task);
  }

  public synchronized FinishLatch execute() {
    final CountDownLatch finishLatch = new CountDownLatch(taskPairs.size());
    for (final Pair<ExecutorService, Runnable> taskPair : taskPairs) {
      taskPair
          .getFirst()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    taskPair.getSecond().run();
                  } finally {
                    finishLatch.countDown();
                  }
                }
              });
    }
    return new FinishLatch(finishLatch);
  }

  public static class FinishLatch {
    private final CountDownLatch finishLatch;

    private FinishLatch(CountDownLatch finishLatch) {
      this.finishLatch = finishLatch;
    }

    public boolean await(long waitTime, TimeUnit waitTimeUnit) throws InterruptedException {
      return finishLatch.await(waitTime, waitTimeUnit);
    }

    public void await() throws InterruptedException {
      finishLatch.await();
    }
  }
}
