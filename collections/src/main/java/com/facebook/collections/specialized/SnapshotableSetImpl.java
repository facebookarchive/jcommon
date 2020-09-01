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
package com.facebook.collections.specialized;

import com.facebook.collections.SetFactory;
import com.facebook.collections.WrappedSet;
import java.util.Set;

/**
 * basic utility class that converts a Set<T> and the appropriate SetFactory into a SnapshotableSet
 *
 * @param <T>
 */
public class SnapshotableSetImpl<T> extends WrappedSet<T> implements SnapshotableSet<T> {

  private final SetFactory<T, SnapshotableSet<T>> setFactory;

  public SnapshotableSetImpl(Set<T> delegate, SetFactory<T, SnapshotableSet<T>> setFactory) {
    super(delegate);
    this.setFactory = setFactory;
  }

  @Override
  public SnapshotableSet<T> makeSnapshot() {
    SnapshotableSet<T> setCopy = setFactory.create();

    // assumes that delegate is either already thread-safe (and therefore
    // "synchronized" is unnecessary) or that it uses the object's monitor to
    // guard access to mutator methods (e.g. Collections.synchronizedSet())
    Set<T> delegate = getDelegate();
    synchronized (delegate) {
      setCopy.addAll(delegate);
    }

    return setCopy;
  }

  @Override
  public SnapshotableSet<T> makeTransientSnapshot() {
    return makeSnapshot();
  }
}
