package com.facebook.concurrency;

import java.util.concurrent.Callable;

public class CallableSnapshotFunctionImpl<I, O, E extends Exception> implements
  CallableSnapshotFunction<I,O,E> {
  private final ValueFactory<I, O, E> valueFactory;
  private final ExceptionHandler<E> exceptionHandler;

  public CallableSnapshotFunctionImpl(
    ValueFactory<I, O, E> valueFactory, ExceptionHandler<E> exceptionHandler
  ) {
    this.valueFactory = valueFactory;
    this.exceptionHandler = exceptionHandler;
  }

  public CallableSnapshotFunctionImpl(ValueFactory<I, O, E> valueFactory) {
    // We can cast exceptions because the value factory declares which type
    // of exceptions it can throw on creation
    this(valueFactory, new CastingExceptionHandler<E>());
  }

  @Override
  public CallableSnapshot<O, E> apply(final I input) {
    return new CallableSnapshot<O, E>(
      new Callable<O>() {
        @Override
        public O call() throws E {
          return valueFactory.create(input);
        }
      },
      exceptionHandler
    );
  }
}
