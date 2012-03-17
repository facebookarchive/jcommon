package com.facebook.zookeeper.election;

public interface LeaderElectionCallback {
  /**
   * Callback signaling that the current host has won the election.
   */
  public void elected();

  /**
   * Callback signaling that the current host was unexpectedly removed from
   * the election or from leadership (e.g. session expiration).
   * Note: Calls to withdraw and cycle will NOT generate removed signals.
   */
  public void removed();

  /**
   * Callback signaling an asynchronous error.
   * @param exception
   */
  public void error(Exception exception);
}
