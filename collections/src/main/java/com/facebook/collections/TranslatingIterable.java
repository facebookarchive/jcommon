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

import java.util.Iterator;

public class TranslatingIterable<X, Y> implements Iterable<Y> {
  private final Mapper<X, Y> mapper;
  private final Iterable<X> iterable;

  public TranslatingIterable(Mapper<X, Y> mapper, Iterable<X> iterable) {
    this.mapper = mapper;
    this.iterable = iterable;
  }

  @Override
  public Iterator<Y> iterator() {
    return new TranslatingIterator<>(mapper, iterable.iterator());
  }
}
