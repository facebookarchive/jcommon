package com.facebook.stats;

import com.facebook.collections.PeekableIterator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks stats over a rolling time period (time window) of maxLength
 * broken into parts (eventCounters list) of size maxChunkLength.
 * Meant to be subclassed.  Primary use is through repeatedly calling
 * add() and occasionally calling getValue().
 * <p/>
 * 1. Does trimming of event buckets (based on the window size)
 * 2. Allows for updates of this window's events based on updates to
 * component windows (useful for overlapping window stats)
 * <p/>
 * Optimized for write-heavy counters.
 */
public abstract class AbstractCompositeCounter<C extends EventCounterIf<C>>
  implements CompositeEventCounterIf<C> {

  // adds/removes to eventCounters happen only when synchronized on "this"
  @GuardedBy("this")
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
      if (eventCounters.isEmpty() || !now.isBefore(eventCounters.getLast().getEnd())) {
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
  public Duration getLength() {
    trimIfNeeded();

    return new Duration(start, end);
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
  public synchronized CompositeEventCounterIf<C> addEventCounter(C eventCounter) {
    if (eventCounters.size() >= 2) {
      mergeChunksIfNeeded();
    }

    // merge above before adding the counter; the invariant is that the
    // added counter should not be merged until it is not the most recent
    // counter
    Preconditions.checkArgument(
      eventCounters.isEmpty() || !eventCounters.getLast().getEnd().isAfter(eventCounter.getEnd()),
      "new counter end , %s, is not past the current end %s",
      eventCounter.getEnd(),
      eventCounters.isEmpty() ? "NaN" : eventCounters.getLast().getEnd()
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

  /**
   * testing to see if we can merge counter1 and counter2 and not violate
   * the maxChunkLength
   * <p/>
   * ...| counter2 | counter1 |
   */
  private void mergeChunksIfNeeded() {
    C counter1 = eventCounters.removeLast();
    C counter2 = eventCounters.getLast();

    if (StatsUtil.extentOf(counter1, counter2).isLongerThan(maxChunkLength)) {
      eventCounters.add(counter1);
    } else {
      eventCounters.removeLast();
      eventCounters.add(counter1.merge(counter2));
    }
  }

  /**
   * This merges another sorted list of counters with our own and produces
   * a new counter.
   * <p/>
   * our own counters are protected from mutation via synchronization. The behavior of this function
   * is not defined if otherCounters changes while a merge is taking place;
   *
   * @param otherCounters usually some other object's counters, or a single counter that's being added
   *                      via addEventCounter()
   * @param mergedCounter
   * @param <C2>
   * @return
   */
  protected synchronized <C2 extends CompositeEventCounterIf<C>> C2 internalMerge(
    Collection<? extends C> otherCounters, C2 mergedCounter
  ) {
    PeekableIterator<C> iter1 = new PeekableIterator<C>(eventCounters.iterator());
    PeekableIterator<C> iter2 = new PeekableIterator<C>(otherCounters.iterator());

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
   * <p/>
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

        // trim any counter with an end up to and including start since our composite counter is
        // [start, ... and each counter is [..., end)
        if (!start.isBefore(counter.getEnd())) {
          iter.remove();
        } else {
          break;
        }
      }
    }
  }

  /**
   * return a copy of current list of event counters; same properties as getEventCounters, but
   * a copy
   *
   * @deprecated see {@link #getEventCounters()} and make a copy externally if a snapshot is needed
   */
  @Deprecated
  protected synchronized List<C> getEventCountersCopy() {
    return new ArrayList<C>(eventCounters);
  }

  /**
   * Get a the current set of event counters. The counters will be
   * sorted in ascending order according to time, meaning the earliest counter will appear first
   * in any iteration
   *
   * @return unmodifiable Collection of event counters
   */
  protected synchronized Collection<C> getEventCounters() {
    return Collections.unmodifiableCollection(eventCounters);
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
   * @return Unmodifiable iterator across windowed event counters in ascending
   *         (oldest first) order
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
   * Create a new counter that is the result of merging this counter and the argument. No deep
   * copy is performed, so the resulting copy could in theory be a counter that just lists
   * this and counter in a list
   *
   * @param counter : other counter to use in merge
   * @return
   */
  @Override
  public abstract C merge(C counter);

  /**
   * callded when a new counter is needed for the range [start, end)
   *
   *
   * @param start
   * @param end
   * @return new counter for range [start, end) to second resolution
   */
  protected abstract C nextCounter(ReadableDateTime start, ReadableDateTime end);
}
