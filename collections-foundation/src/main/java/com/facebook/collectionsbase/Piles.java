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
package com.facebook.collectionsbase;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * utility methods for working with Piles (collections) of elements.
 */
public class Piles {
    private Piles() {
      throw new AssertionError();
    }

  /**
   * works with guava's Function interface
   *
   * @param iterator
   * @param function
   * @param <X>
   * @param <Y>
   * @return
   */
  public static <X, Y> List<Y> transmogrify(Iterator<X> iterator, Function<X, Y> function) {
    com.facebook.collectionsbase.Mapper<X, Y> mapper = new com.facebook.collectionsbase.FunctionToMapper<X, Y>(function);

    return transmogrify(iterator, mapper);
  }

  /**
   * creates a list of type Y from an iterator of type X
   *
   * @param iterator
   * @param mapper
   * @param <X>
   * @param <Y>
   * @return
   */
  public static <X, Y> List<Y> transmogrify(Iterator<X> iterator, Mapper<X, Y> mapper) {
    List<Y> result = new ArrayList<>();

    transmogrify(iterator, result, mapper);

    return result;
  }

  /**
   * real basic, just make the iterator into a list
   *
   * @param iterator
   * @param <T>
   * @return
   */
  public static <T> List<T> copyOf(Iterator<T> iterator) {
    List<T> result = new ArrayList<T>();

    copyOf(iterator, result);

    return result;
  }

  public static <X, Y> Collection<Y> transmogrify(
    Iterator<X> iterator, Collection<Y> target, Function<X, Y> function
  ) {
    FunctionToMapper<X, Y> mapper = new FunctionToMapper<>(function);

    transmogrify(iterator, target, mapper);

    return target;
  }
  /**
   * allows caller to provide a Collection to place the iterator into
   * @param iterator
   * @param target
   * @param <T>
   * @return
   */
  public static <X, Y> Collection<Y> transmogrify(
    Iterator<X> iterator, Collection<Y> target, Mapper<X, Y> mapper
  ) {
    while (iterator.hasNext()) {
      target.add(mapper.map(iterator.next()));
    }

    return target;
  }

/**
   * allows caller to provide a Collection to place the iterator into
   * @param iterator
   * @param target
   * @param <T>
   * @return
   */

  public static <T> Collection<T> copyOf(Iterator<T> iterator, Collection<T> target) {
    while (iterator.hasNext()) {
      target.add(iterator.next());
    }

    return target;
  }

  /**
   *
   * @param source input collection
   * @param target collection containing filtered items
   * @param filter
   * @param <T> item type
   * @param <C> collection type
   * @param <E>
   * @return target
   * @throws E
   */
  public static <T, C extends Collection<T>, E extends Throwable> C filter(Collection<T> source, C target, Filter<T,E> filter)
    throws E {
    for (T item : source) {
      if (filter.execute(item)) {
        target.add(item);
      }
    }

    return target;
  }
}
