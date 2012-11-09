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
