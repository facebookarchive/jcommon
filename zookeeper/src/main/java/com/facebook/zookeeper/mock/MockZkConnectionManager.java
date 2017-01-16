/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  private final List<Watcher> watchers = new ArrayList<>();
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
