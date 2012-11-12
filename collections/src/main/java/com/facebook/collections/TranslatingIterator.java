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

public class TranslatingIterator<X, Y> implements Iterator<Y> {
  private final Mapper<X, Y> mapper;
  private final Iterator<X> iterator;

  public TranslatingIterator(Mapper<X, Y> mapper, Iterator<X> iterator) {
    this.mapper = mapper;
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Y next() {
    X input = iterator.next();
    
    return mapper.map(input);
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
