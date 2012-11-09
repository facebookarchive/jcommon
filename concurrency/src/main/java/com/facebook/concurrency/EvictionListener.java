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
 * 
 * @param <K>
 * @param <V>
 */
public interface EvictionListener<K, V> {
  /**
   * notifies a listener that a key/value pair has been evicted. No guarantee
   * is made that the key/value pair is not re-inserted by the time this 
   * occurs (ie, an eviction occurred in the past)
   * @param key key that was evicted 
   * @param value value evicted. May be null in the case that either null
   * was inserted, or an exception was thrown producing the value
   */
  void evicted(K key, V value);
}
