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
