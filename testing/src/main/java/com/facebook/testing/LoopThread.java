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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * used only with ThreadHelper
 */
public class LoopThread {
  private final Thread thread;
  private final AtomicBoolean condition;

  /**
   * @param thread
   * @param condition serves as simple Observer.  Will be set to false
   * when someone tries to calls join()
   * 
   * often used by the underlying Runnable to know that someone may
   * be ready for this to terminate
   */
  LoopThread(Thread thread, AtomicBoolean condition) {
    this.thread = thread;
    this.condition = condition;
  }

  public void start() {
    thread.start();
  }
  
  public String getName() {
    return thread.getName();
  }

  public void join() throws InterruptedException {
    condition.set(false);
    thread.join();
  }
}
