package com.facebook.collections;

public interface SnapshotProvider<T> {
  /**
   * Make the latest snapshot.
   *
   * @return  the latest snapshot of T
   */
  public T makeSnapshot();

  /**
   * same as above, but the implementation may use alternative data structures
   * to improve cpu efficiency over memory since the caller is indicating
   * this copy will be short-lived
   * 
   * @return
   */
  public T makeTransientSnapshot();
}
