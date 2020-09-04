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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RetrieveableSet<T> implements Set<T> {
  private final Map<T, T> identityMap;

  public RetrieveableSet(Map<T, T> identityMap) {
    this.identityMap = identityMap;
  }

  public RetrieveableSet() {
    this(new HashMap<>());
  }

  public T get(T t) {
    return identityMap.get(t);
  }

  @Override
  public int size() {
    return identityMap.size();
  }

  @Override
  public boolean isEmpty() {
    return identityMap.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return identityMap.containsKey(o);
  }

  @Override
  public Iterator<T> iterator() {
    return identityMap.keySet().iterator();
  }

  @Override
  public Object[] toArray() {
    return identityMap.keySet().toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return identityMap.keySet().toArray(ts);
  }

  @Override
  public boolean add(T t) {
    if (identityMap.containsKey(t)) {
      return false;
    }
    identityMap.put(t, t);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    return identityMap.remove(o) != null;
  }

  @Override
  public boolean containsAll(Collection<?> objects) {
    return identityMap.keySet().containsAll(objects);
  }

  @Override
  public boolean addAll(Collection<? extends T> ts) {
    boolean changed = false;
    for (T t : ts) {
      changed |= add(t);
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> objects) {
    return identityMap.keySet().retainAll(objects);
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    return identityMap.keySet().removeAll(objects);
  }

  @Override
  public void clear() {
    identityMap.clear();
  }
}
