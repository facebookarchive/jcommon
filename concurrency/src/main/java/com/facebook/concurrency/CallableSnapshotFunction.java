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

/**
 * This is very similar to a factory, but the apply() function implementations
 * usually create a Callable which is then executed. The resuting value, or
 * exception, is then stored in the CallableSnapshot
 *  
 * @param <I> type of the input to pass to underlying implementation. Used in
 * creating the Callable
 * @param <O> output of the implementation's Callable
 * @param <E> exception type that may be thrown by the Callable
 */
public interface CallableSnapshotFunction<I, O, E extends Exception> {
  CallableSnapshot<O, E> apply(I input);
}
