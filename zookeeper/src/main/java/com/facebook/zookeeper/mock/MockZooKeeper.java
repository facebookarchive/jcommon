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


import com.facebook.zookeeper.ZooKeeperIface;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class MockZooKeeper implements ZooKeeperIface {
  private final MockZooKeeperDataStore dataStore;
  private final long sessionId;
  private final Watcher defaultWatcher;
  private ZooKeeper.States state = ZooKeeper.States.CONNECTING;

  public MockZooKeeper(Watcher watcher, MockZooKeeperDataStore dataStore) {
    this.dataStore = dataStore;
    this.sessionId = dataStore.getUniqueSessionId();
    this.defaultWatcher = watcher;
  }

  public synchronized void triggerConnect() {
    state = ZooKeeper.States.CONNECTED;
    WatchedEvent watchedEvent =
      new WatchedEvent(EventType.None, KeeperState.SyncConnected, null);
    dataStore.signalSessionEvent(sessionId, watchedEvent);
    defaultWatcher.process(watchedEvent);
  }

  public synchronized void triggerDisconnect() {
    state = ZooKeeper.States.CONNECTING;
    WatchedEvent watchedEvent =
      new WatchedEvent(EventType.None, KeeperState.Disconnected, null);
    dataStore.signalSessionEvent(sessionId, watchedEvent);
    defaultWatcher.process(watchedEvent);
  }

  public synchronized void triggerSessionExpiration() {
    state = ZooKeeper.States.CLOSED;
    WatchedEvent watchedEvent =
      new WatchedEvent(EventType.None, KeeperState.Expired, null);
    dataStore.signalSessionEvent(sessionId, watchedEvent);
    defaultWatcher.process(watchedEvent);
    dataStore.clearSession(sessionId);
  }

  @Override
  public long getSessionId() {
    return sessionId;
  }

  @Override
  public synchronized void close() throws InterruptedException {
    state = ZooKeeper.States.CLOSED;
    dataStore.clearSession(sessionId);
  }

  private void verifyConnected() throws KeeperException {
    if (state == ZooKeeper.States.CLOSED) {
      throw new KeeperException.SessionExpiredException();
    }
    if (state != ZooKeeper.States.CONNECTED) {
      throw new KeeperException.ConnectionLossException();
    }
  }

  @Override
  public synchronized String create(String path, byte[] data, List<ACL> acl, CreateMode createMode)
    throws KeeperException, InterruptedException {
    verifyConnected();
    return dataStore.create(sessionId, path, data, acl, createMode);
  }

  @Override
  public synchronized void delete(String path, int expectedVersion)
    throws InterruptedException, KeeperException {
    verifyConnected();
    dataStore.delete(path, expectedVersion);
  }

  @Override
  public synchronized Stat exists(String path, Watcher watcher)
    throws KeeperException, InterruptedException {
    verifyConnected();
    return dataStore.exists(sessionId, path, watcher);
  }

  @Override
  public synchronized Stat exists(String path, boolean watch)
    throws KeeperException, InterruptedException {
    return exists(path, watch ? defaultWatcher : null);
  }

  @Override
  public synchronized byte[] getData(String path, Watcher watcher, Stat stat)
    throws KeeperException, InterruptedException {
    verifyConnected();
    return dataStore.getData(sessionId, path, watcher, stat);
  }

  @Override
  public synchronized byte[] getData(String path, boolean watch, Stat stat)
    throws KeeperException, InterruptedException {
    return getData(path, watch ? defaultWatcher : null, stat);
  }

  @Override
  public synchronized Stat setData(String path, byte[] data, int expectedVersion)
    throws KeeperException, InterruptedException {
    verifyConnected();
    return dataStore.setData(path, data, expectedVersion);
  }

  @Override
  public synchronized List<String> getChildren(String path, Watcher watcher)
    throws KeeperException, InterruptedException {
    verifyConnected();
    return dataStore.getChildren(sessionId, path, watcher);
  }

  @Override
  public synchronized List<String> getChildren(String path, boolean watch)
    throws KeeperException, InterruptedException {
    return getChildren(path, watch ? defaultWatcher : null);
  }

  @Override
  public synchronized ZooKeeper.States getState() {
    return state;
  }
}
