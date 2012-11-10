package com.facebook.zookeeper.connection;

import com.facebook.concurrency.ErrorLoggingRunnable;
import com.facebook.concurrency.NamedThreadFactory;
import com.facebook.zookeeper.ZooKeeperFactory;
import com.facebook.zookeeper.ZooKeeperIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZkConnectionManagerImpl implements ZkConnectionManager {
  private static final Logger LOG =
    LoggerFactory.getLogger(ZkConnectionManagerImpl.class);

  private final ZooKeeperFactory zooKeeperFactory;
  private final List<Watcher> registeredWatchers =
    new CopyOnWriteArrayList<Watcher>();
  private final ConnectionWatcher connectionWatcher = new ConnectionWatcher();
  private final ConnectionRenewer connectionRenewer =
    new ConnectionRenewer();
  private final int connectTimeoutMillis;
  private final int retryIntervalMillis;
  private volatile ZooKeeperIface zk = null;
  private volatile boolean isStarted = false;
  private volatile boolean isShutDown = false;

  public ZkConnectionManagerImpl(
    ZooKeeperFactory zooKeeperFactory,
    int connectTimeoutMillis,
    int retryIntervalMillis
  ) {
    this.zooKeeperFactory = zooKeeperFactory;
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.retryIntervalMillis = retryIntervalMillis;
  }

  // Instance must be started before it becomes valid for use
  public synchronized void start() {
    if (isStarted) {
      throw new IllegalStateException("Should only be started once");
    }
    // Connect to the ZooKeeper cluster
    try {
      // Call connect first to guarantee zk is not null when this method exits
      connect();
    } catch (IOException e) {
      connectionRenewer.activate();
    }
    isStarted = true;
  }

  private void verifyOperational() {
    if (!isStarted) {
      throw new IllegalStateException("Not yet started");
    }
    if (isShutDown) {
      throw new IllegalStateException("Already closed");
    }
  }

  private boolean isAlive() {
    return zk != null && zk.getState().isAlive();
  }

  private synchronized void connect() throws IOException {
    if (!isShutDown && !isAlive()) {
      LOG.info("Initializing ZooKeeper connection");
      connectionWatcher.reset();
      zk = zooKeeperFactory.create(connectionWatcher);
    }
  }

  @Override
  public ZooKeeperIface getClient() throws InterruptedException {
    verifyOperational();
    // Wait if we are in the process of connecting
    if (!connectionWatcher.waitForConnect(connectTimeoutMillis, TimeUnit.MILLISECONDS)) {
      LOG.error("Exceeded " + connectTimeoutMillis + " ms waiting for " +
        "connection to be established! Using disconnected client...");
    }
    return zk;
  }

  @Override
  public ZooKeeper.States registerWatcher(Watcher watcher) {
    // This operation may legitimately happen at any time without problems

    // Note: the setting of the watch MUST precede the reading of the state
    // in order to ensure that the caller doesn't miss any watches after
    // getting the state.
    registeredWatchers.add(watcher);
    return (zk == null) ? ZooKeeper.States.CLOSED : zk.getState();
  }

  public synchronized void shutdown() throws InterruptedException {
    LOG.info("Closing ZooKeeper connection");
    verifyOperational();
    try {
      connectionRenewer.shutdown();
      if (isAlive()) {
        zk.close();
      }
    } finally {
      isShutDown = true;
    }
  }

  /**
   * When activated, will try to connect to ZooKeeper until it succeeds.
   */
  private class ConnectionRenewer {
    private final ScheduledExecutorService retryExecutor =
      Executors.newSingleThreadScheduledExecutor(
        new NamedThreadFactory("ZkConnectionManager-renewer")
      );
    private final AtomicBoolean isScheduled = new AtomicBoolean(false);

    public void activate() {
      if (isScheduled.compareAndSet(false, true)) {
        retryExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
          @Override
          public void run() {
            try {
              connect();
              return; // Success, don't reschedule
            } catch (IOException e) {
              // Try again after sleeping...
              LOG.error("Failed to connect to ZooKeeper", e);
            } finally {
              isScheduled.set(false);
            }
            if (isScheduled.compareAndSet(false, true)) {
              retryExecutor.schedule(
                this, retryIntervalMillis, TimeUnit.MILLISECONDS
              );
            }
          }
        }));
      }
    }

    public void shutdown() {
      retryExecutor.shutdown();
    }
  }

  private class ConnectionWatcher implements Watcher {
    private volatile CountDownLatch connectedSignal = new CountDownLatch(1);

    public synchronized void reset() {
      connectedSignal.countDown(); // Unblock any prior threads
      connectedSignal = new CountDownLatch(1);
    }

    public boolean waitForConnect(int timeout, TimeUnit timeUnit)
      throws InterruptedException {
      return connectedSignal.await(timeout, timeUnit);
    }

    @Override
    public void process(WatchedEvent event) {
      // Handle the connection event signals. No thread needed because all
      // operations are non-blocking
      switch (event.getState()) {
        case SyncConnected:
          LOG.info("ZooKeeper connected");
          connectedSignal.countDown();
          break;
        case Disconnected:
          LOG.warn("ZooKeeper disconnected!");
          break;
        case Expired:
          LOG.warn("ZooKeeper session expired!");
          // Reset connection to the ZooKeeper cluster
          connectionRenewer.activate();
          break;
      }

      // Signal the registered watchers with the connection event
      for (Watcher watcher : registeredWatchers) {
        try {
          // All watchers should have non-blocking implementations
          watcher.process(event);
        } catch (Throwable t) {
          LOG.error("Registered watcher failed handling connection event", t);
        }
      }
    }
  }
}
