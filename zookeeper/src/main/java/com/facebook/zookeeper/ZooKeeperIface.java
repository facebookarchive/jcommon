package com.facebook.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooKeeper.States;

import java.util.List;

/**
 * ZooKeeper interface limited to some basic operations. More ZooKeeper methods
 * may be added to this interface as needed.
 * See ZooKeeper API for documentation.
 */
public interface ZooKeeperIface {
  long getSessionId();

  void close() throws InterruptedException;

  String create(String path, byte[] data, List<ACL> acl, CreateMode createMode)
    throws KeeperException, InterruptedException;

  void delete(String path, int version)
    throws InterruptedException, KeeperException;

  Stat exists(String path, Watcher watcher)
    throws KeeperException, InterruptedException;

  Stat exists(String path, boolean watch)
    throws KeeperException, InterruptedException;

  byte[] getData(String path, Watcher watcher, Stat stat)
    throws KeeperException, InterruptedException;

  byte[] getData(String path, boolean watch, Stat stat)
    throws KeeperException, InterruptedException;

  Stat setData(String path, byte[] data, int version)
    throws KeeperException, InterruptedException;

  List<String> getChildren(String path, Watcher watcher)
    throws KeeperException, InterruptedException;

  List<String> getChildren(String path, boolean watch)
    throws KeeperException, InterruptedException;

  States getState();
}
