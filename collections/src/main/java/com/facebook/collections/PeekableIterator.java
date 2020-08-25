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
   * will use the 'cached' value from a peek if available
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
