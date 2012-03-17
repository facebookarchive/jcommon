package com.facebook.zookeeper.election;

import org.apache.zookeeper.KeeperException;

public interface LeaderElectionObserver {
  /**
   * Gets the name of the candidate that is currently the leader
   * @return Name of the leader, or null if there are no candidates
   * @throws InterruptedException
   * @throws org.apache.zookeeper.KeeperException
   */
  String getLeader() throws InterruptedException, KeeperException;
}
