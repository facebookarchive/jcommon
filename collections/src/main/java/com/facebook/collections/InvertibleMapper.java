package com.facebook.collections;

import com.facebook.collectionsbase.Mapper;

public interface InvertibleMapper<X, Y> extends Mapper<X,Y> {
  public X unmap(Y input);
}
