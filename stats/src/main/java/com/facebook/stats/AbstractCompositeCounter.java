package com.facebook.stats;

import com.facebook.collections.PeekableIterator;
import com.google.common.collect.Iterators;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks stats over a rolling time period (time window) of maxLength
 * broken into parts (eventCounters list) of size maxChunkLength.
 * Meant to be subclassed.  Primary use is through repeatedly calling
 * add() and occasionally calling getValue().
 *
 * 1. Does trimming of event buckets (based on the window size)
 * 2. Allows for updates of this window's events based on updates to
 *    component windows (useful for overlapping window stats)
 *
 * Optimized for write-heavy counters.
 */
public abstract class AbstractCompositeCounter<C extends EventCounterIf<C>>
  implements CompositeEventCounterIf<C> {

  private final Deque<C> eventCounters = new ArrayDeque<C>();
  private final ReadableDuration maxLength;      // total window size
  private final ReadableDuration maxChunkLength; // size per counter

  private ReadableDateTime start;
  private ReadableDateTime end;

  /*
   * Create a CompositeCounter of window size maxLength broken into
   * individual eventCounters of size maxChunkLength.
   */
  public AbstractCompositeCounter(
    ReadableDuration maxLength, ReadableDuration maxChunkLength
  ) {
    this.maxLength = maxLength;
    this.maxChunkLength = maxChunkLength;

    DateTime now = new DateTime();

    start = now;
    end = now;
  }

  public AbstractCompositeCounter(ReadableDuration maxLength) {
    this(maxLength, new Duration(maxLength.getMillis() / 10));
  }

  /**
   * Adds the value to the counter, and may create a new eventCounter
   * to store the value if needed.
   */
  @Override
  public void add(long delta) {
    DateTime now = new DateTime();
    C last;

    synchronized (this) {
      if (eventCounters.isEmpty() ||
        !now.isBefore(eventCounters.getLast().getEnd())) {
        addEventCounter(nextCounter(now, now.plus(maxChunkLength)));
      }

      last = eventCounters.getLast();
    }

    last.add(delta);
  }


  public ReadableDateTime getStart() {
    trimIfNeeded();

    return start;
  }

  public ReadableDateTime getEnd() {
    trimIfNeeded();

    return end;
  }

  @Override
  public synchronized CompositeEventCounterIf<C> add(
    long delta, ReadableDateTime start, ReadableDateTime end
  ) {
    C counter = nextCounter(start, end);

    counter.add(delta);

    return addEventCounter(counter);
  }

  @Override
  public synchronized CompositeEventCounterIf<C> addEventCounter(
      C eventCounter
  ) {
    if (eventCounters.size() >= 2) {
      mergeChunksIfNeeded();
    }
    // merge above before adding the counter; the invariant is that the
    // added counter should not be merged until it is not the most recent
    // counter
    assert(
      eventCounters.isEmpty() ||
        !eventCounters.getLast().getEnd().isAfter(eventCounter.getEnd())
    );

    eventCounters.add(eventCounter);
    if (eventCounter.getStart().isBefore(start)) {
      start = eventCounter.getStart();
      trimIfNeeded();
    }

    if (eventCounter.getEnd().isAfter(end)) {
      end = eventCounter.getEnd();
      trimIfNeeded();
    }
    return this;
  }

  private void mergeChunksIfNeeded() {
    C counter1 = eventCounters.removeLast();
    C counter2 = eventCounters.getLast();

    if (StatsUtil.extentOf(counter1, counter2)
      .isLongerThan(maxChunkLength)
      ) {
      eventCounters.add(counter1);
    } else {
      eventCounters.removeLast();
      eventCounters.add(counter1.merge(counter2));
    }
  }

  /**
   * This merges another sorted list of counters with our own and produces
   * a new counter
   *
   * @param otherEventCounters - list of counters sorted by start to merge
   * @return new CompositeEventCounter containing both sets of counters
   */
  protected synchronized final <C2 extends CompositeEventCounterIf<C>> C2
  internalMerge(
    Collection<? extends C> counters1,
    Collection<? extends C> counters2,
    C2 mergedCounter
  ) {
    PeekableIterator<C> iter1 = new PeekableIterator<C>(counters1.iterator());
    PeekableIterator<C> iter2 = new PeekableIterator<C>(counters2.iterator());

    while (iter1.hasNext() || iter2.hasNext()) {
      if (iter1.hasNext() && iter2.hasNext()) {
        // take the counter that occurs first and merge it
        if (iter1.peekNext().getStart().isBefore(iter2.peekNext().getStart())) {
          mergedCounter.addEventCounter(iter1.next());
        } else {
          mergedCounter.addEventCounter(iter2.next());
        }
      } else if (iter1.hasNext()) {
        mergedCounter.addEventCounter(iter1.next());
      } else if (iter2.hasNext()) {
        mergedCounter.addEventCounter(iter2.next());
      }
    }

    return mergedCounter;
  }

  /**
   * Updates the current composite counter so that it is up to date with the
   * current timestamp.
   *
   * This should be called by any method that needs to have the most updated
   * view of the current set of counters.
   */
  protected synchronized void trimIfNeeded() {
    Duration delta = new Duration(start, new DateTime())
      .minus(maxLength);

    if (delta.isLongerThan(Duration.ZERO)) {
      start = start.toDateTime().plus(delta);

      if (start.isAfter(end)) {
        end = start;
      }

      Iterator<C> iter = eventCounters.iterator();

      while (iter.hasNext()) {
        EventCounterIf<C> counter = iter.next();

        if (counter.getEnd().isBefore(start)) {
          iter.remove();
        } else {
          break;
        }
      }
    }
  }

  /**
   * Get a copy of the current set of event counters. The counters will be
   * sorted in time increasing order with the first counter being the earliest.
   *
   * @return List of event counters
   */
  protected synchronized List<C> getEventCountersCopy() {
    return new ArrayList<C>(eventCounters);
  }

  /**
   * Returns the most recently added counter or null if does not exist
   *
   * @return EventCounter
   */
  protected synchronized C getMostRecentCounter() {
    return eventCounters.peekLast();
  }

  /**
   * Callers of this method MUST be synchronized while iterating
   *
   * @return Unmodifiable iterator across windowed event counters
   */
  protected Iterator<C> eventCounterIterator() {
    return Iterators.unmodifiableIterator(eventCounters.iterator());
  }

  protected ReadableDateTime getWindowStart() {
    return start;
  }

  protected ReadableDateTime getWindowEnd() {
    return end;
  }

  protected ReadableDuration getMaxLength() {
    return maxLength;
  }

  protected ReadableDuration getMaxChunkLength() {
    return maxChunkLength;
  }

  /**
   * Create a copy of this counter's member counters and add the RHS
   * counter
   *
   * @param counter : other counter to use in merge
   * @return
   */
  @Override
  public abstract C merge(C counter);

  /**
   * Used when a new counter is needed
   *
   * @param start
   * @param end
   * @return
   */
  protected abstract C nextCounter(ReadableDateTime start, ReadableDateTime end);
}
