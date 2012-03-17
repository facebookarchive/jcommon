package com.facebook.collections;

public interface InvertibleMapper<X, Y> extends Mapper<X,Y>{
  public X unmap(Y input);
}
