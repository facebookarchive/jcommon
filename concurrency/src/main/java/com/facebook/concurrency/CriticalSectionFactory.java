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

import java.util.concurrent.atomic.AtomicBoolean;

public class CriticalSectionFactory {
  private final AtomicBoolean isRunning = new AtomicBoolean(false);

  public Runnable wrap(Runnable runnable) {
    return new Runnable() {
      @Override
      public void run() {
        if (isRunning.compareAndSet(false, true)) {
          try {
            runnable.run();
          } finally {
            isRunning.set(false);
          }
        }
      }
    };
  }
}
