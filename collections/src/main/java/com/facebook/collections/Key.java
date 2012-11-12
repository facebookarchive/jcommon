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

import com.google.common.reflect.TypeToken;

public class Key<K, V> {
  private K name;
  private TypeToken<V> type;

  public Key(K id, TypeToken<V> type) {
    this.name = id;
    this.type = type;
  }

  public Key(K id, Class<V> clazz) {
    this.name = id;
    this.type = TypeToken.of(clazz);
  }

  public static <K, V> Key<K, V> get(K id, Class<V> clazz) {
    return new Key<>(id, clazz);
  }

  public static <K, V> Key<K, V> get(K id, TypeToken<V> clazz) {
    return new Key<>(id, clazz);
  }

  public K getName() {
    return name;
  }

  public TypeToken<V> getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Key key = (Key) o;

    if (!type.equals(key.type)) {
      return false;
    }
    if (!name.equals(key.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
