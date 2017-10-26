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
package com.facebook.util;

import com.facebook.util.exceptions.UncheckedCheckedException;

/**
 * extended runnable that throws an exception. Does not a produce a value
 * (hence not a Callable)
 * 
 * @param <E> type of exception you want to be able to throw
 */
public interface ExtRunnable<E extends Throwable>{
  void run() throws E;
  
  static Runnable quiet(ExtRunnable<?> runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (Error | RuntimeException e) {
        throw e;
      } catch (Throwable e) {
        throw new UncheckedCheckedException(e);
      }
    };
  }
}
