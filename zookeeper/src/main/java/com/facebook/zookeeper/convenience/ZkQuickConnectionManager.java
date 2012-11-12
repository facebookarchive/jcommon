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

import com.facebook.zookeeper.ZooKeeperIface;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ZkQuickConnectionManager {
  private ZkQuickConnection zkConnection;

  public void connect(String server, int sessionTimeout)
    throws IOException, InterruptedException, TimeoutException {
    zkConnection = new ZkQuickConnection(server, sessionTimeout);
  }

  public void connect(String server, int sessionTimeout, int connTimeout)
    throws IOException, InterruptedException, TimeoutException {
    zkConnection = new ZkQuickConnection(server, sessionTimeout, connTimeout);
  }

  public void close() throws InterruptedException {
    if (zkConnection != null) {
      zkConnection.close();
    }
  }

  public ZooKeeperIface getZk() {
    if (zkConnection == null) {
      throw new IllegalStateException(
        "You must connect before you can get the ZooKeeper client"
      );
    }
    return zkConnection.getZk();
  }
}
