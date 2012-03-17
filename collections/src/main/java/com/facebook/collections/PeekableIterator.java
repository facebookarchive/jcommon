package com.facebook.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * convenience class that allows peekNext() to get the next value while still
 * allowing the same hasNext() and next() semantics
 * 
 * @param <T> type that the iterator returns on next()
 */
public class PeekableIterator<T> implements Iterator<T> {
  private final Iterator<? extends T> delegate;
  private T value = null;

  public PeekableIterator(Iterator<? extends T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext() || value != null;
  }

  /**
   * will use the 'cached' value from a peek if availble
   * @return
   * @throws NoSuchElementException
   */
  @Override
  public T next() throws NoSuchElementException {
    T retVal;

    if (value == null) {
      internalNext();
    }
    
    retVal = value;
    
    value = null;
    
    return retVal;
  }
  
  private void internalNext() throws NoSuchElementException {
    value = delegate.next();
  }
  
  public T peekNext() {
    if (value == null) {
      internalNext();
    }
    
    return value;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove not supported");
  }
}
