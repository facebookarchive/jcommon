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
package com.facebook.zookeeper.convenience;

import com.facebook.zookeeper.BasicZooKeeperFactory;
import com.facebook.zookeeper.ZooKeeperFactory;
import com.facebook.zookeeper.ZooKeeperIface;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ZkQuickConnection {
  private final ZooKeeperIface zk;

  private ZkQuickConnection(
    ZooKeeperFactory zooKeeperFactory,
    int connectionTimeoutMillis
  ) throws IOException, InterruptedException, TimeoutException {
    final CountDownLatch latch = new CountDownLatch(1);
    zk = zooKeeperFactory.create(new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
          latch.countDown();
        }
      }
    });
    if (!latch.await(connectionTimeoutMillis, TimeUnit.MILLISECONDS)) {
      zk.close();
      throw new TimeoutException("Failed to connect to ZooKeeper");
    }
  }

  public ZkQuickConnection(
    String server, int zkTimeoutMillis, int connectTimeoutMillis
  ) throws IOException, InterruptedException, TimeoutException {
    this(
      new BasicZooKeeperFactory(server, zkTimeoutMillis),
      connectTimeoutMillis
    );
  }

  public ZkQuickConnection(
    String server, int zkTimeoutMillis
  ) throws IOException, InterruptedException, TimeoutException {
    this(server, zkTimeoutMillis, 10000);
  }

  public ZooKeeperIface getZk() {
    return zk;
  }

  public void close() throws InterruptedException {
    zk.close();
  }
}
