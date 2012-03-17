package com.facebook.stats;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Simple class to periodically gather a service's counters and cache them.
 * How to use:
 *
 * When your thrift service initializes:
 *
 *   this.counterCacher = new CounterCacher(this);
 *   this.counterCacher.run();
 *
 * Then implement getCounters() as something like:
 *
 *   public Map<String, Long> getCounters() {
 *     return this.counterCacher.getCounters();
 *   }
 *
 * Make sure you implement your own counter-generating code inside
 * makeCounters(), and have it call super.makeCounters() so that fb303 can
 * do the same.
 *
 */

public class CounterCacher {
  private static class ThreadFactory implements java.util.concurrent.ThreadFactory {
    long count = 0;

    public Thread newThread(Runnable r) {
      count ++;
      return new Thread(threadGroup, r, threadGroup.getName() + "-" + count);
    }
  }

  private class CounterCacherRunner implements Runnable {
    private final long minWait;
    private final long maxWait;

    public CounterCacherRunner(long minWait, long maxWait) {
      this.minWait = minWait;
      this.maxWait = maxWait;
    }

    public void run() {
      final Logger log = Logger.getLogger(CounterCacher.class.getCanonicalName());
      wantRunning = true;
      running = true;

      log.log(Level.INFO, "Cacheing counters every " + minWait + " - " + maxWait + " msec");

      try {
        while(wantRunning) {
          try {
            long startTime = System.currentTimeMillis();
            counters = reporter.makeCounters();
            Thread.sleep(minWait);
            long runTime = System.currentTimeMillis() - startTime;
            long remainingWait = maxWait - runTime;
            if(remainingWait > 0) {
              Thread.sleep(remainingWait);
            }
          } catch(InterruptedException iex) {
            wantRunning = false;
          }
        }
      } catch(RuntimeException rex) {
        log.log(Level.SEVERE, "RuntimeException thrown while running makeCounters()", rex);
      } finally {
        running = false;
      }
    }
  }

  private final static ThreadGroup threadGroup = new ThreadGroup("CounterCacher");
  private final static ThreadFactory threadFactory = new ThreadFactory();


  private volatile Thread thread;
  private final Runnable runnable;
  private final FacebookStatsReporter reporter;

  private volatile boolean running = false;
  private volatile boolean wantRunning = false;

  private volatile Map<String, Long> counters;

  /**
   * @param  FacebookStatsReporter    Your service
   * @param  long minWait             Minimum time to wait between calls
   *                                  to makeCounters (default=1000, or 1s)
   * @param  long maxWait             Maximum time to wait between calls
   *                                  to makeCounters (default=1000, or 1s)
   *
   * Example:
   *
   * If it takes 2 seconds to make your counters, minWait is 1 second,
   * and maxWait is 10 seconds then there will be an 8 second delay
   * between calls. If it takes 15 seconds to make your counters, there
   * will be a 1 second delay.
   */
  public CounterCacher(final FacebookStatsReporter reporter, long minWait, long maxWait) {
    runnable = new CounterCacherRunner(minWait, maxWait);
    this.reporter = reporter;
  }

  public CounterCacher(FacebookStatsReporter reporter, long minWait) {
    this(reporter, minWait, minWait);
  }

  public CounterCacher(FacebookStatsReporter reporter) {
    this(reporter, 1000);
  }

  public void start() {
    if(running) {
      throw new IllegalStateException("start() called while already running!");
    }

    thread = threadFactory.newThread(runnable);
    thread.start();
  }

  public void stop() {
    if(!running) {
      throw new IllegalStateException("stop() called while not running!");
    }

    wantRunning = false;
    thread.interrupt();
    try {
      thread.join();
    } catch(InterruptedException iex) { }
    thread = null;
  }

  public Map<String, Long> getCounters() {
    return counters;
  }
}

