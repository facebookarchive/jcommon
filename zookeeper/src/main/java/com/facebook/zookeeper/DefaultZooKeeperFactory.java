package com.facebook.zookeeper;

import org.apache.zookeeper.Watcher;

import java.io.IOException;

public class DefaultZooKeeperFactory implements ZooKeeperFactory {
  private final String zkHostStr;
  private final int timeoutMillis;
  private final int maxRetries;
  private final int retryIntervalMillis;

  public DefaultZooKeeperFactory(
    String zkHostStr,
    int timeoutMillis,
    int maxRetries,
    int retryIntervalMillis
  ) {
    this.zkHostStr = zkHostStr;
    this.timeoutMillis = timeoutMillis;
    this.maxRetries = maxRetries;
    this.retryIntervalMillis = retryIntervalMillis;
  }

  public DefaultZooKeeperFactory(String zkHostStr, int timeoutMillis) {
    this(zkHostStr, timeoutMillis, 3, 1000);
  }

  @Override
  public ZooKeeperIface create(Watcher watcher) throws IOException {
    ZooKeeperFactory sinkZooKeeperFactory =
      new BasicZooKeeperFactory(zkHostStr, timeoutMillis);

    ZooKeeperFactory recoveringZooKeeperFactory =
      new RecoveringZooKeeperFactory(
        sinkZooKeeperFactory, maxRetries, retryIntervalMillis
      );

    return recoveringZooKeeperFactory.create(watcher);
  }
}
