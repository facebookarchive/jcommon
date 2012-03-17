package com.facebook.zookeeper.connection;

import com.facebook.zookeeper.ZooKeeperIface;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * The ZkConnectionManager initiates a connection to the ZooKeeper cluster
 * at initialization time and maintains this connection until shutdown.
 */
public interface ZkConnectionManager {

  /**
   * Returns a valid ZooKeeperIface client that may be used to communicate
   * with the ZooKeeper cluster.
   *
   * @return Valid ZooKeeperIface client
   * @throws InterruptedException
   */
  public ZooKeeperIface getClient() throws InterruptedException;

  /**
   * Registers an additional watcher to receive ZooKeeper connection
   * event callbacks.
   * @param watcher
   * @return ZooKeeper state at the instant or just after the watch is set
   */
  public ZooKeeper.States registerWatcher(Watcher watcher);

  /**
   * Kills any active connections and shuts down the ZooKeeper connection
   * manager.
   */
  public void shutdown() throws InterruptedException;

}
