package com.facebook.config;

// interface that both indicates annotated fields for how to construct a builder
// fromJSON as well as a method to construct a specified object type (T)
public interface ExtractableBeanBuilder<T> {
  public T build();
}
