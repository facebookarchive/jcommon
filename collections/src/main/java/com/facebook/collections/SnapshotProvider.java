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
package com.facebook.collections;

public interface SnapshotProvider<T> {
  /**
   * Make the latest snapshot.
   *
   * @return  the latest snapshot of T
   */
  public T makeSnapshot();

  /**
   * same as above, but the implementation may use alternative data structures
   * to improve cpu efficiency over memory since the caller is indicating
   * this copy will be short-lived
   * 
   * @return
   */
  public T makeTransientSnapshot();
}
