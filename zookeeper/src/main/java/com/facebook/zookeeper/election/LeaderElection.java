package com.facebook.zookeeper.election;

import org.apache.zookeeper.KeeperException;

public interface LeaderElection extends LeaderElectionObserver {
  /**
   * Adds the current host to the election if it is not there already. May be
   * safely re-executed following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void enter() throws InterruptedException, KeeperException;

  /**
   * Completely removes the candidate from the election. May be safely
   * re-executed following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void withdraw() throws InterruptedException, KeeperException;

  /**
   * Relinquishes the candidate's current standing (possibly as the leader) and
   * demotes the candidate to the last position. May be safely re-executed
   * following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void cycle() throws InterruptedException, KeeperException;
}
