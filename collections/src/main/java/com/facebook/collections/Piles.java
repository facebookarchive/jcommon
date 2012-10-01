package com.facebook.collections;

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

  public static <S, T extends Collection<S>> Class<T> compose(Class<T> classT, Class<S> clazzS) {
    return classT;
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
    Mapper<X, Y> mapper = new FunctionToMapper<>(function);

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
}
