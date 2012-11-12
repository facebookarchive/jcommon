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
import org.apache.zookeeper.Watcher;

import java.io.IOException;

/**
 * All MockZooKeepers constructed by the same factory will share the same
 * MockZooKeeperDataStore. This simulates multiple clients connecting to the
 * same ZooKeeper qorum.
 */
public class MockZooKeeperFactory implements ZooKeeperFactory {
  private final MockZooKeeperDataStore dataStore;
  private MockZooKeeper lastZk;
  private boolean throwOnNextCreate = false;
  private boolean autoConnect = false;

  public MockZooKeeperFactory(MockZooKeeperDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public MockZooKeeper getLastZooKeeper() {
    return lastZk;
  }

  public void throwOnNextCreate() {
    throwOnNextCreate = true;
  }

  public void setAutoConnect(boolean autoConnect) {
    this.autoConnect = autoConnect;
  }

  @Override
  public ZooKeeperIface create(Watcher watcher) throws IOException {
    if (throwOnNextCreate) {
      throwOnNextCreate = false;
      throw new IOException();
    }
    lastZk = new MockZooKeeper(watcher, dataStore);
    if (autoConnect) {
      lastZk.triggerConnect();
    }
    return lastZk;
  }
}
