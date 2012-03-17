package com.facebook.zookeeper;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class BasicZooKeeperFactory implements ZooKeeperFactory {
  private final String hosts;
  private final int timeout;

  public BasicZooKeeperFactory(String hosts, int timeout) {
    this.hosts = hosts;
    this.timeout = timeout;
  }

  @Override
  public ZooKeeperIface create(Watcher watcher) throws IOException {
    return new BasicZooKeeper(new ZooKeeper(hosts, timeout, watcher));
  }
}
