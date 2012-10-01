package com.facebook.collections;

import com.google.common.base.Function;

public class FunctionToMapper<X, Y> implements Mapper<X, Y> {
  private final Function<X, Y> function;

  public FunctionToMapper(Function<X, Y> function) {
    this.function = function;
  }

  @Override
  public Y map(X input) {
    return function.apply(input);
  }
}
