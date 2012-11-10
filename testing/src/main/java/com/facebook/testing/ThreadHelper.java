package com.facebook.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * class useful in unit tests
 */
public class ThreadHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadHelper.class);
  
  private final List<Throwable> exceptionList = new ArrayList<Throwable>();
  
  public Thread doInThread(Runnable operation) {
    return doInThread(operation, null);
  }
  
  public Thread doInThread(Runnable operation, String threadName) {
    Thread t = new Thread(operation);

    // need to make sure we propagate the exception
    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        exceptionList.add(e);
      }
    });

    if (threadName != null) {
      t.setName(threadName);
    }

    t.start();

    return t;
  }

  public LoopThread repeatInThread(final Runnable operation) {
    return repeatInThread(operation, null);
  }

  public LoopThread repeatInThread(final Runnable operation, String threadName) {
    final AtomicBoolean shouldRun = new AtomicBoolean(true);
    Runnable loopTask = new Runnable() {
      @Override
      public void run() {
        while (shouldRun.get()) {
          try {
            operation.run();
          } catch (Throwable t) {
            LOG.error("error running task", t);
          }
        }
      }
    };
    
    Thread t = doInThread(loopTask, threadName);

    return new LoopThread(t, shouldRun);
  }

  public List<Throwable> getExceptionList() {
    return exceptionList;
  }
  
}
