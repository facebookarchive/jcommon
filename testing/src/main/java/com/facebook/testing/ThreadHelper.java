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
package com.facebook.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** class useful in unit tests */
public class ThreadHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadHelper.class);

  private final List<Throwable> exceptionList = new ArrayList<>();

  public Thread doInThread(Runnable operation) {
    return doInThread(operation, null);
  }

  public Thread doInThread(Runnable operation, String threadName) {
    Thread t = new Thread(operation);

    // need to make sure we propagate the exception
    t.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            exceptionList.add(e);
          }
        });

    if (threadName != null) {
      t.setName(threadName);
    }

    t.start();

    return t;
  }

  public LoopThread repeatInThread(final Runnable operation) {
    return repeatInThread(operation, null);
  }

  public LoopThread repeatInThread(final Runnable operation, String threadName) {
    final AtomicBoolean shouldRun = new AtomicBoolean(true);
    Runnable loopTask =
        new Runnable() {
          @Override
          public void run() {
            while (shouldRun.get()) {
              try {
                operation.run();
              } catch (Throwable t) {
                LOG.error("error running task", t);
              }
            }
          }
        };

    Thread t = doInThread(loopTask, threadName);

    return new LoopThread(t, shouldRun);
  }

  public List<Throwable> getExceptionList() {
    return exceptionList;
  }
}
