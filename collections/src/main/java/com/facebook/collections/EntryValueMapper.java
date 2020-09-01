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

import com.facebook.collectionsbase.Mapper;
import java.util.Map;
import java.util.Map.Entry;

/**
 * maps a Map.Entry<K,V> to V
 *
 * @param <K> key type of entry
 * @param <V> value type of entry
 */
public class EntryValueMapper<K, V> implements Mapper<Entry<K, V>, V> {
  @Override
  public V map(Map.Entry<K, V> input) {
    return input.getValue();
  }
}
