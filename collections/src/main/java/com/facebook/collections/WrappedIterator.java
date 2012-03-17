package com.facebook.collections;

import java.util.Iterator;

public class WrappedIterator<T> implements Iterator<T> {
  private final Iterator<T> delegate;

  public WrappedIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public T next() {
    return delegate.next();
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
