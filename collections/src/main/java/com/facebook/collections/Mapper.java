package com.facebook.collections;

public interface Mapper<X, Y> {
  public Y map(X input);
}
