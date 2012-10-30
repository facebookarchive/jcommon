package com.facebook.collectionsbase;

public interface Mapper<X, Y> {
  public Y map(X input);
}
