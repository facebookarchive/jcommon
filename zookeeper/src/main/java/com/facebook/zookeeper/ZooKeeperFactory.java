package com.facebook.zookeeper;

import org.apache.zookeeper.Watcher;

import java.io.IOException;

/**
 * Interface that describes methods to create ZooKeeperIface implementing
 * classes.
 */
public interface ZooKeeperFactory {
  /**
   * Creates a ZooKeeperIface instance
   * @param watcher - Watcher to monitor changes in connection status
   * @return new ZooKeeperIface instance
   * @throws IOException
   */
  ZooKeeperIface create(Watcher watcher) throws IOException;
}
