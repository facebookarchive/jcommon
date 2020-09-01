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
import java.util.Iterator;
import java.util.Set;

public class WrappedSet<T> implements Set<T> {
  private final Set<T> delegate;

  public WrappedSet(Set<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return delegate.iterator();
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  public boolean add(T t) {
    return delegate.add(t);
  }

  @Override
  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  public boolean addAll(Collection<? extends T> c) {
    return delegate.addAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof WrappedSet) {
      Set otherDelegate = ((WrappedSet) o).getDelegate();

      return delegate.equals(otherDelegate) || delegate.equals(o);
    }
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  protected Set<T> getDelegate() {
    return delegate;
  }
}
