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
package com.facebook.concurrency.linearization;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

/**
 * The idea here is that we want to impose a partial ordering on
 * a series of tasks. This class allows you to generate "Points" that
 * have a start() and complete() method.  LinearizationPoints cannot
 * start() until all previously created Points have called complete().
 * A ConcurrentPoint may not start until the last LinearizationPoint has
 * called complete();
 *
 * The following example guarantees that printing of point3 will happen
 * after point1 and point2, though the former two can come in any order
 *
 * <pre>
* {@code

    ExecutorService executor = Executors.newCachedThreadPool();
    Linearizer linearizer = new Linearizer();
    final ConcurrentPoint concurrentPoint1 = linearizer.createConurrentPoint();
    final ConcurrentPoint concurrentPoint2 = linearizer.createConurrentPoint();
    Runnable task1 = new Runnable() {
      @Override
      public void run() {
        concurrentPoint1.start();

        try {
          System.err.println("point1");
        } finally {
          concurrentPoint1.complete();
        }
      }
    };
    Runnable task2 = new Runnable() {
      @Override
      public void run() {
         concurrentPoint2.start();

        try {
          System.err.println("point2");
        } finally {
          concurrentPoint2.complete();
        }
      }
    };
    executor.execute(task1);
    executor.execute(task2);

    final LinearizationPoint linearizationPoint =
      linearizer.createLinearizationPoint();
    Runnable task3 = new Runnable() {
      @Override
      public void run() {
        linearizationPoint.start();

        try {
          System.err.println("point3");
        } finally {
          linearizationPoint.complete();
        }
      }
    };

    executor.execute(task3);
    executor.shutdown();
  }
 </pre>
 */
public class Linearizer {
  private static final Logger LOG = LoggerImpl.getLogger(Linearizer.class);
  private static final long COMPLETE_WAIT_TIME_SECONDS = 300;

  private final AtomicReference<AtomicInteger> pointCountRef =
    new AtomicReference<AtomicInteger>(new AtomicInteger(0));
  private final AtomicReference<LinearizationPoint> lastLinearizationPointRef =
    new AtomicReference<LinearizationPoint>();

  /**
   * creates an lock-object such that other objects of this type
   * may interleave their start/complete calls.
   *
   * calling start() on the resulting ConcurrentPoint will block until
   * the previous LinearizationPoint calls complete()
   *
   *
   * @return
   */
  public synchronized ConcurrentPoint createConcurrentPoint() {
    return new ConcurrentPointImpl(
      pointCountRef.get(), lastLinearizationPointRef.get()
    );
  }

  /**
   * calling start() on the resulting LinearizationPoint will block
   * until all previously generated Points call complete()
   *
   * @return
   */
  public synchronized LinearizationPoint createLinearizationPoint() {
    AtomicInteger nextPointCount = new AtomicInteger();
    AtomicInteger previousPointCount = pointCountRef.getAndSet(nextPointCount);

    LinearizationPointImpl linearizationPoint =
      new LinearizationPointImpl(previousPointCount, nextPointCount);

    // set this so that subsequently generated ConcurrentPoints can
    // call linearizationPoint.waitForCompletion()
    lastLinearizationPointRef.set(linearizationPoint);

    return linearizationPoint;
  }

  private static class ConcurrentPointImpl implements ConcurrentPoint {
    private final AtomicInteger pointCount;
    private final LinearizationPoint previousLinearizationPoint;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    private ConcurrentPointImpl(
      AtomicInteger pointCount, LinearizationPoint previousLinearizationPoint
    ) {
      pointCount.incrementAndGet();
      this.previousLinearizationPoint = previousLinearizationPoint;
      this.pointCount = pointCount;
    }

    @Override
    public void start() {
      try {
        // if there is a previous LinearizationPoint, we cannot
        // start until it completesv
        if (previousLinearizationPoint != null) {
          previousLinearizationPoint.waitForCompletion();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(
          "interrupted waiting for previous LinearizationPoint to complete"
        );
      }
    }

    @Override
    public void complete() {
      if (completed.compareAndSet(false, true)) {
        int result = pointCount.decrementAndGet();
        // if we are the last point in a virtual queue, signal any
        // LinearizationPoint that might be waiting on said virtual queue
        if (result == 0) {
          synchronized (pointCount) {
            pointCount.notifyAll();
          }
        }
      }
    }
  }

  private static class LinearizationPointImpl implements LinearizationPoint {
    private final CountDownLatch startSignal = new CountDownLatch(1);
    private final CountDownLatch completeSignal = new CountDownLatch(1);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicInteger previousPointCount;
    private final AtomicInteger nextPointCount;

    private LinearizationPointImpl(
      AtomicInteger previousPointCount,
      AtomicInteger nextPointCount
    ) {
      // we have to increment this so that if another LinearizationPoint
      // is generated after us, it won't start until we complete
      nextPointCount.incrementAndGet();
      this.nextPointCount = nextPointCount;
      this.previousPointCount = previousPointCount;
    }

    private void waitUntilPreviousPointsComplete() {
      try {
        synchronized (previousPointCount) {
          while (previousPointCount.get() > 0) {
            previousPointCount.wait(5000);
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(
          "interrupted waiting for ConcurrentPoints", e)
          ;
      }
    }


    @Override
    public void start() {
      // we wait for any points in the previous virtual queue to complete
      // By transitivity, this means *all* previous points will be complete
      // by this point
      waitUntilPreviousPointsComplete();
      startSignal.countDown();
    }

    @Override
    public void complete() {
      if (completed.compareAndSet(false, true)) {
        int result = nextPointCount.decrementAndGet();
        // if we are the last point in a virtual queue, signal any
        // LinearizationPoint that might be waiting on said virtual queue
        if (result == 0) {
          synchronized (nextPointCount) {
            nextPointCount.notifyAll();
          }
        }

        completeSignal.countDown();
      }
    }

    @Override
    public void waitForStart() throws InterruptedException {
      while (!startSignal.await(COMPLETE_WAIT_TIME_SECONDS, TimeUnit.SECONDS)) {
        LOG.info(
          "waited %d seconds for LinearizationPoint.start, will wait some more",
          COMPLETE_WAIT_TIME_SECONDS
        );
      }
    }

    @Override
    public boolean waitForStart(long timeout, TimeUnit unit)
      throws InterruptedException {

      return startSignal.await(timeout, unit);
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
      while (!completeSignal.await(COMPLETE_WAIT_TIME_SECONDS, TimeUnit.SECONDS)) {
        LOG.info(
          "waited %d seconds for LinearizationPoint.complete, will wait some more",
          COMPLETE_WAIT_TIME_SECONDS
        );
      }
    }

    @Override
    public boolean waitForCompletion(long timeout, TimeUnit unit)
      throws InterruptedException {

      return completeSignal.await(timeout, unit);
    }
  }
}
