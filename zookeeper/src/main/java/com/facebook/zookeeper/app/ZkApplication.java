package com.facebook.zookeeper.app;

import com.facebook.concurrency.ErrorLoggingRunnable;
import com.facebook.concurrency.NamedThreadFactory;
import com.facebook.zookeeper.connection.ZkConnectionManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ZkApplication is an abstract base class that provides a template-and-hook
 * style ZooKeeper state management framework.
 *
 * Features:
 * - Automatic connection and reconnection to ZooKeeper on disconnects and
 *   expirations.
 * - Issues initialize(), repair(), and expire() callbacks to the application
 *   as various connection events occur.
 * - Models application state with a finite state machine. States are queryable
 *   from subclasses.
 *
 * =============================== STATES ===============================
 *
 * PRESTART:
 * This is the application's initial state before the user issues a start()
 * command to begin the application
 *
 * DISCONNECTED:
 * Application is not connected to ZooKeeper and does not have any
 * application state set in ZooKeeper (e.g. watches or ephemeral nodes).
 * Entry to this state automatically triggers a connection loop that retries
 * until successful.
 *
 * CONNECTED:
 * Application is connected to ZooKeeper, but application state in ZooKeeper
 * has not been fully initialized. Entry to this state automatically triggers
 * callbacks to the initialize() abstract method, looping until initialize()
 * returns true.
 *
 * FUNCTIONAL:
 * Application is connected to ZooKeeper and has full application state
 * initialized in ZooKeeper. At this point the application should be fully
 * functioning.
 *
 * SAFEMODE:
 * Application was successfully initialized, but became disconnected. Since
 * many applications will cache ZooKeeper state, this state signifies a
 * cache read-only mode where ZooKeeper is unavailable. Entry to this
 * state will automatically trigger a connection loop that retries until
 * reconnected, or until the session expires.
 *
 * SAFEMODE_REPAIR:
 * Application in safemode was successfully reconnected to ZooKeeper without
 * expiration. Entry to this state will automatically trigger callbacks to the
 * repair() abstract method, looping until repair() returns true. While
 * initialize() and repair() may do the same things, we make this distinction
 * as it is often possible to optimize the repair method to only repeat failed
 * commands.
 *
 * SHUTDOWN:
 * Application has been shut down by the user via the shutdown() method.
 *
 * ======================= STATE TRANSITION DIAGRAM =====================
 *
 * The state transition diagram appears as follows:
 *
 *                  PRESTART     (expire*)
 *                     | (start)    |
 *                     v            |      
 *                DISCONNECTED <----       -->  SAFEMODE
 *                     | (connect)        /        | (connect)
 *                     v                 /         v
 *                 CONNECTED    (dc'ed) /    SAFEMODE_REPAIR
 *                       \             /          /
 *                        \           /          /
 *                 (init)  \         /          / (repair)
 *                          \       /          /
 *                           v     /          /
 *                          FUNCTIONAL <-----
 *
 *
 *                  (shutdown*) -----> SHUTDOWN
 *
 * NOTE: '*' denotes an event that unconditionally leads to a specific state,
 * regardles of the pre-existing state.
 */
public abstract class ZkApplication {
  private static final Logger APP_LOG = Logger.getLogger(ZkApplication.class);
  
  protected final ZkConnectionManager zkConnectionManager;
  private final ExecutorService watchExecutor;
  private final ScheduledExecutorService retryExecutor;
  private final long retryIntervalMillis;
  private final CountDownLatch initLatch = new CountDownLatch(1);
  private final StateContext context = new StateContext();
  private volatile boolean isStarted = false;

  public enum State {
    PRESTART,
    DISCONNECTED,
    CONNECTED,
    FUNCTIONAL,
    SAFEMODE,
    SAFEMODE_REPAIR,
    SHUTDOWN,
  }

  // This constructor only exposes the executors for unit testing purposes
  protected ZkApplication(
    ZkConnectionManager zkConnectionManager,
    long retryIntervalMillis,
    ExecutorService watchExecutor,
    ScheduledExecutorService retryExecutor
  ) {
    this.zkConnectionManager = zkConnectionManager;
    this.retryIntervalMillis = retryIntervalMillis;
    this.watchExecutor = watchExecutor;
    this.retryExecutor = retryExecutor;
  }

  protected ZkApplication(
    ZkConnectionManager zkConnectionManager, long retryIntervalMillis
  ) {
    this(
      zkConnectionManager,
      retryIntervalMillis,
      Executors.newSingleThreadExecutor(
        new NamedThreadFactory("ZkApplication-watch")
      ),
      Executors.newSingleThreadScheduledExecutor(
        new NamedThreadFactory("ZkApplication-retry")
      )
    );
  }

  protected ZkApplication(ZkConnectionManager zkConnectionManager) {
    this(zkConnectionManager, 2000);
  }

  // Instance must be started before it becomes valid for use
  public synchronized void start() {
    if (isStarted) {
      throw new IllegalStateException("Should only be started once");
    }
    ZooKeeper.States zkState =
      zkConnectionManager.registerWatcher(new ConnectionWatcher());
    // Synchronize our application state with ZooKeeper state
    context.start((zkState == ZooKeeper.States.CONNECTED)
      ? State.CONNECTED
      : State.DISCONNECTED
    );
    isStarted = true;
    // Allow watch signals to pass only after initialization
    initLatch.countDown();
  }

  public boolean isFunctional() {
    return context.getState() == State.FUNCTIONAL;
  }

  public boolean isSafeMode() {
    return context.getState() == State.SAFEMODE ||
      context.getState() == State.SAFEMODE_REPAIR;
  }

  public boolean isShutdown() {
    return context.getState() == State.SHUTDOWN;
  }

  public synchronized void shutdown() {
    if (!isStarted) {
      throw new IllegalStateException("Application not yet started");
    }
    context.shutdown();
    watchExecutor.shutdown();
    retryExecutor.shutdown();
  }

  // Derived classes need to provide implementations for the following methods:

  /**
   * Initializes the application such that it is fully functioning.
   * Implementations should be idempotent, and will be retried repeatedly
   * until it succeeds.
   * @return true if successfully initialized, false otherwise
   */
  protected abstract boolean initialize();

  /**
   * Repair the application state to a fully functioning state following
   * a ZooKeeper disconnect. Implementations should be idempotent, and will
   * be retried repeatedly until it succeeds.
   * @return true if successfully repaired, false otherwise
   */
  protected abstract boolean repair();

  /**
   * Cleans up application state as necessary following a ZooKeeper session
   * expiration. Implementations should be idempotent.
   */
  protected abstract void expire();


  // Internal helper classes

  private class ConnectionWatcher implements Watcher {
    @Override
    public void process(final WatchedEvent event) {
      watchExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
        @Override
        public void run() {
          try {
            initLatch.await(); // Wait until we have been fully initialized
          } catch (InterruptedException e) {
            APP_LOG.error("Init latch interrupted, continuing...");
            Thread.currentThread().interrupt();
          }
          context.handleEvent(event.getState());
        }
      }));
    }
  }

  private interface StateHandler {
    void handleEvent(Watcher.Event.KeeperState event);
    void inboundHook();
    void outboundHook();
  }

  private class StateContext {
    private volatile State state = State.PRESTART;
    private final Object transitionLock = new Object();
    private final Map<State, StateHandler> handlerCache =
      new EnumMap<State, StateHandler>(State.class);

    private StateContext() {
      // Should be one entry per possible state
      handlerCache.put(State.PRESTART, new PreStartStateHandler());
      handlerCache.put(State.DISCONNECTED, new DisconnectedStateHandler());
      handlerCache.put(State.CONNECTED, new ConnectedStateHandler());
      handlerCache.put(State.FUNCTIONAL, new FunctionalStateHandler());
      handlerCache.put(State.SAFEMODE, new SafeModeStateHandler());
      handlerCache.put(State.SAFEMODE_REPAIR, new SafeModeRepairStateHandler());
      handlerCache.put(State.SHUTDOWN, new ShutdownStateHandler());
    }

    // Instance must be started before it becomes valid for use
    public void start(State initialState) {
      synchronized (transitionLock) {
        transition(initialState);
      }
    }

    public void handleEvent(Watcher.Event.KeeperState event) {
      synchronized (transitionLock) {
        getHandler().handleEvent(event);
      }
    }

    public void shutdown() {
      synchronized (transitionLock) {
        if (state == State.SHUTDOWN) {
          APP_LOG.warn("Multiple shutdown calls");
          return;
        }
        internalTransition(State.SHUTDOWN);
      }
    }

    public State getState() {
      return state;
    }

    private StateHandler getHandler() {
      return handlerCache.get(state);
    }

    private void transition(State newState) {
      if (newState == State.SHUTDOWN) {
        throw new IllegalArgumentException(
          "Set SHUTDOWN state by calling the shutdown method"
        );
      }
      // Shutdown is a terminal state
      if (state != State.SHUTDOWN) {
        internalTransition(newState);
      }
    }

    private void internalTransition(State newState) {
      getHandler().outboundHook();
      state = newState;
      getHandler().inboundHook();
    }


    // StateHandler implementations

    private class PreStartStateHandler implements StateHandler {
      @Override
      public void handleEvent(Watcher.Event.KeeperState event) {
      }

      @Override
      public void inboundHook() {
      }

      @Override
      public void outboundHook() {
      }
    }

    private class DisconnectedStateHandler implements StateHandler {
      @Override
      public void handleEvent(Watcher.Event.KeeperState event) {
        switch (event) {
          case SyncConnected:
            context.transition(State.CONNECTED);
            break;
          case Disconnected:
            break;
          case Expired:
            break;
        }
      }

      @Override
      public void inboundHook() {
      }

      @Override
      public void outboundHook() {
      }
    }

    private class ConnectedStateHandler implements StateHandler {
      private volatile boolean isActive = false;
      private volatile boolean isScheduled = false;
      private final Object scheduleCheckLock = new Object();

      @Override
      public void handleEvent(Watcher.Event.KeeperState event) {
        switch (event) {
          case SyncConnected:
            break;
          case Disconnected:
            context.transition(State.DISCONNECTED);
            break;
          case Expired:
            context.transition(State.DISCONNECTED);
            expire();
            break;
        }
      }

      @Override
      public void inboundHook() {
        startInitLoop();
      }

      private void startInitLoop() {
        isActive = true;
        if (scheduleCompareAndSet()) {
          retryExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
            @Override
            public void run() {
              try {
                synchronized (transitionLock) {
                  // Only initialize if we are in the same state
                  if (context.getState() == State.CONNECTED) {
                    // Attempt initialization
                    if (initialize()) {
                      context.transition(State.FUNCTIONAL);
                      return; // Success, don't reschedule
                    }
                  }
                }
              } finally {
                isScheduled = false;
              }
              if (scheduleCompareAndSet()) {
                retryExecutor.schedule(
                  this, retryIntervalMillis, TimeUnit.MILLISECONDS
                );
              }
            }
          }));
        }
      }

      private boolean scheduleCompareAndSet() {
        synchronized (scheduleCheckLock) {
          if (isActive && !isScheduled) {
            isScheduled = true;
            return true;
          }
          return false;
        }
      }

      @Override
      public void outboundHook() {
        stopRepairLoop();
      }

      private void stopRepairLoop() {
        isActive = false;
      }
    }

    private class FunctionalStateHandler implements StateHandler {
      @Override
      public void handleEvent(Watcher.Event.KeeperState event) {
        switch (event) {
          case SyncConnected:
            break;
          case Disconnected:
            context.transition(State.SAFEMODE);
            break;
          case Expired:
            context.transition(State.DISCONNECTED);
            expire();
            break;
        }
      }

      @Override
      public void inboundHook() {
      }

      @Override
      public void outboundHook() {
      }
    }

    private class SafeModeStateHandler implements StateHandler {
      public void handleEvent(Watcher.Event.KeeperState event) {
        switch (event) {
          case SyncConnected:
            context.transition(State.SAFEMODE_REPAIR);
            break;
          case Disconnected:
            break;
          case Expired:
            context.transition(State.DISCONNECTED);
            expire();
            break;
        }
      }

      @Override
      public void inboundHook() {
      }

      @Override
      public void outboundHook() {
      }
    }

    private class SafeModeRepairStateHandler implements StateHandler {
      private volatile boolean isActive = false;
      private volatile boolean isScheduled = false;
      private final Object scheduleCheckLock = new Object();

      public void handleEvent(Watcher.Event.KeeperState event) {
        switch (event) {
          case SyncConnected:
            break;
          case Disconnected:
            context.transition(State.SAFEMODE);
            break;
          case Expired:
            context.transition(State.DISCONNECTED);
            expire();
            break;
        }
      }

      @Override
      public void inboundHook() {
        startRepairLoop();
      }

      private void startRepairLoop() {
        isActive = true;
        if (scheduleCompareAndSet()) {
          retryExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
            @Override
            public void run() {
              try {
                synchronized (transitionLock) {
                  // Only repair if we are still in the same state
                  if (context.getState() == State.SAFEMODE_REPAIR) {
                    // Attempt repair
                    if (repair()) {
                      context.transition(State.FUNCTIONAL);
                      return;  // Success, don't reschedule
                    }
                  }
                }
              } finally {
                isScheduled = false;
              }
              if (scheduleCompareAndSet()) {
                retryExecutor.schedule(
                  this, retryIntervalMillis, TimeUnit.MILLISECONDS
                );
              }
            }
          }));
        }
      }

      private boolean scheduleCompareAndSet() {
        synchronized (scheduleCheckLock) {
          if (isActive && !isScheduled) {
            isScheduled = true;
            return true;
          }
          return false;
        }
      }

      @Override
      public void outboundHook() {
        stopRepairLoop();
      }

      private void stopRepairLoop() {
        isActive = false;
      }
    }

    private class ShutdownStateHandler implements StateHandler {
      @Override
      public void handleEvent(Watcher.Event.KeeperState event) {
        // Terminal state
      }

      @Override
      public void inboundHook() {
        expire();
      }

      @Override
      public void outboundHook() {
      }
    }
  }
}
