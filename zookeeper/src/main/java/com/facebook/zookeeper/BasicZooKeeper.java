package com.facebook.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class BasicZooKeeper implements ZooKeeperIface {
  private final ZooKeeper zk;

  public BasicZooKeeper(ZooKeeper zk) {
    this.zk = zk;
  }

  @Override
  public long getSessionId() {
    return zk.getSessionId();
  }

  @Override
  public void close() throws InterruptedException {
    zk.close();
  }

  @Override
  public String create(String path, byte[] data, List<ACL> acl, CreateMode createMode)
    throws KeeperException, InterruptedException {
    return zk.create(path, data, acl, createMode);
  }

  @Override
  public void delete(String path, int version)
    throws InterruptedException, KeeperException {
    zk.delete(path, version);
  }

  @Override
  public Stat exists(String path, Watcher watcher)
    throws KeeperException, InterruptedException {
    return zk.exists(path, watcher);
  }

  @Override
  public Stat exists(String path, boolean watch)
    throws KeeperException, InterruptedException {
    return zk.exists(path, watch);
  }

  @Override
  public byte[] getData(String path, Watcher watcher, Stat stat)
    throws KeeperException, InterruptedException {
    return zk.getData(path, watcher, stat);
  }

  @Override
  public byte[] getData(String path, boolean watch, Stat stat)
    throws KeeperException, InterruptedException {
    return zk.getData(path, watch, stat);
  }

  @Override
  public Stat setData(String path, byte[] data, int version)
    throws KeeperException, InterruptedException {
    return zk.setData(path, data, version);
  }

  @Override
  public List<String> getChildren(String path, Watcher watcher)
    throws KeeperException, InterruptedException {
    return zk.getChildren(path, watcher);
  }

  @Override
  public List<String> getChildren(String path, boolean watch)
    throws KeeperException, InterruptedException {
    return zk.getChildren(path, watch);
  }

  @Override
  public ZooKeeper.States getState() {
    return zk.getState();
  }
}
