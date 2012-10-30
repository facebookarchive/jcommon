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
