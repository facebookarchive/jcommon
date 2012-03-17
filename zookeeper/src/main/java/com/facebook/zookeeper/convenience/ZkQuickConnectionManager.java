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
