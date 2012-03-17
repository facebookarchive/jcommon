package com.facebook.zookeeper.mock;

import com.facebook.zookeeper.ZooKeeperFactory;
import com.facebook.zookeeper.ZooKeeperIface;
import com.facebook.zookeeper.connection.ZkConnectionManager;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

public class MockZkConnectionManager implements ZkConnectionManager {
  private final ZooKeeperFactory zooKeeperFactory;
  private ZooKeeperIface zk;
  private final List<Watcher> watchers = new ArrayList<Watcher>();
  private boolean isShutdown = false;

  public MockZkConnectionManager(ZooKeeperFactory zooKeeperFactory) {
    this.zooKeeperFactory = zooKeeperFactory;
    refreshClient();
  }

  public void refreshClient() {
    try {
      if (zk != null) {
        zk.close();
      }
      zk = zooKeeperFactory.create(new Watcher() {
        @Override
        public void process(WatchedEvent event) {
          for (Watcher watcher : watchers) {
            watcher.process(event);
          }
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ZooKeeperIface getClient() throws InterruptedException {
    if (isShutdown) {
      throw new IllegalStateException("Already shutdown");
    }
    return zk;
  }

  @Override
  public ZooKeeper.States registerWatcher(Watcher watcher) {
    watchers.add(watcher);
    return zk.getState();
  }

  @Override
  public void shutdown() throws InterruptedException {
    if (isShutdown) {
      throw new IllegalStateException("Multiple shutdowns");
    }
    isShutdown = true;
  }
}
