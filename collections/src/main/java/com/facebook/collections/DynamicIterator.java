package com.facebook.collections;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class DynamicIterator<T> extends AbstractIterator<T> {

  private static final int DEFAULT_INITIAL_SIZE = 16;

  private final Semaphore available = new Semaphore(0);

  @GuardedBy("queue")
  private final Queue<T> queue;

  @GuardedBy("queue")
  private boolean finished;

  public DynamicIterator(int initialSize) {
    Preconditions.checkArgument(initialSize >= 0, "initialSize must be >= 0");

    this.queue = new ArrayDeque<T>(initialSize);
  }

  public DynamicIterator() {
    this(DEFAULT_INITIAL_SIZE);
  }

  public boolean add(T element) throws InterruptedException {
    Preconditions.checkNotNull(element, "element is null");

    boolean added;
    synchronized (queue) {
      Preconditions.checkState(!finished, "%s already finished", getClass().getName());
      added = queue.add(element);
    }

    if (added) {
      available.release();
    }

    return added;
  }

  public void finish() {
    synchronized (queue) {
      finished = true;
    }

    // Do one final release to wake up the consumer if it's waiting.
    // The consumer will see a "null" as the last element, indicating we're done
    available.release();
  }

  @Override
  protected T computeNext() {
    try {
      available.acquire();

      synchronized (queue) {
        T element = queue.poll();
        if (element == null) {
          Preconditions.checkState(finished, "Expected iterator to be finished");
          endOfData();
        }
        return element;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw Throwables.propagate(e);
    }
  }

}
