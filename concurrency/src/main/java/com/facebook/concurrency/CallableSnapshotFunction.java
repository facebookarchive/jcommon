package com.facebook.concurrency;

/**
 * This is very similar to a factory, but the apply() function implementations
 * usually create a Callable which is then executed. The resuting value, or
 * exception, is then stored in the CallableSnapshot
 *  
 * @param <I> type of the input to pass to underlying implementation. Used in
 * creating the Callable
 * @param <O> output of the implementation's Callable
 * @param <E> exception type that may be thrown by the Callable
 */
public interface CallableSnapshotFunction<I, O, E extends Exception> {
  CallableSnapshot<O, E> apply(I input);
}
