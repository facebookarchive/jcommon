package com.facebook.stats;

import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

import java.util.Arrays;

/**
 * stats class that is useful for tracking the number of events that occur
 * in a given time window.
 * <p/>
 * 1. Does trimming of events based on the window size
 * 2. Allows for updates of this window's events based on updates to
 * component windows (useful for overlapping window stats)
 * <p/>
 * Optimal for write-heavy counters
 */
public class CompositeSum extends AbstractCompositeSum<EventCounter>
  implements EventCounter {

  public CompositeSum(
    ReadableDuration maxLength, ReadableDuration maxChunkLength
  ) {
    super(maxLength, maxChunkLength);
  }

  public CompositeSum(ReadableDuration maxLength) {
    super(maxLength);
  }

  @Override
  protected EventCounter nextCounter(
    ReadableDateTime start, ReadableDateTime end
  ) {
    return new EventCounterImpl(start, end);
  }

  @Override
  public EventCounter merge(EventCounter counter) {
    // special case to handle merging of 2 composite counters
    if (counter instanceof CompositeSum) {
      return internalMerge(
        ((CompositeSum) counter).getEventCounters(),
        new CompositeSum(getMaxLength(), getMaxChunkLength())
      );
    } else {
      return internalMerge(
        Arrays.asList(counter),
        new CompositeSum(getMaxLength(), getMaxChunkLength())
      );
    }
  }
}
