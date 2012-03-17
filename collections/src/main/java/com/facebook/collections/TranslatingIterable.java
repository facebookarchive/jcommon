package com.facebook.collections;

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
    return new TranslatingIterator<X,Y>(mapper, iterable.iterator());
  }
}
