package com.facebook.concurrency;

public interface ValueFactory<I, O, E extends Exception> {
  /**
   * Given some input of type I, creates an output object of type O, or throws
   * an exception of type E if there was some trouble creating it.
   * @param input
   * @return
   * @throws E
   */
  public O create(I input) throws E;
}
