package com.facebook.zookeeper;

import org.apache.zookeeper.Watcher;

import java.io.IOException;

public class RecoveringZooKeeperFactory implements ZooKeeperFactory {
  private final ZooKeeperFactory zooKeeperFactory;
  private final int maxRetries;
  private final int retryIntervalMillis;

  public RecoveringZooKeeperFactory(
    ZooKeeperFactory zooKeeperFactory, int maxRetries, int retryIntervalMillis
  ) {
    this.zooKeeperFactory = zooKeeperFactory;
    this.maxRetries = maxRetries;
    this.retryIntervalMillis = retryIntervalMillis;
  }

  @Override
  public ZooKeeperIface create(Watcher watcher) throws IOException {
    return new RecoveringZooKeeper(
      zooKeeperFactory.create(watcher), maxRetries, retryIntervalMillis
    );
  }
}
