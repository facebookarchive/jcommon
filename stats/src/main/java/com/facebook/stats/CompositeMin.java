package com.facebook.stats;

import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

import java.util.Arrays;
import java.util.Iterator;

public class CompositeMin extends AbstractCompositeCounter<EventCounter>
  implements EventCounter {

  public CompositeMin(
    ReadableDuration maxLength, ReadableDuration maxChunkLength
  ) {
    super(maxLength, maxChunkLength);
  }

  public CompositeMin(ReadableDuration maxLength) {
    super(maxLength);
  }

  @Override
  public EventCounter merge(EventCounter counter) {
    if (counter instanceof CompositeMin) {
      return internalMerge(
        ((CompositeMin) counter).getEventCounters(),
        new CompositeMin(getMaxLength(), getMaxChunkLength())
      );
    } else {
      return internalMerge(
        Arrays.asList(counter),
        new CompositeMin(getMaxLength(), getMaxChunkLength())
      );
    }
  }

  @Override
  protected EventCounter nextCounter(
    ReadableDateTime start, ReadableDateTime end
  ) {
    return new MinEventCounter(start, end);
  }

  @Override
  public synchronized long getValue() {
    trimIfNeeded();

    long min = Long.MAX_VALUE;

    for (EventCounter eventCounter : getEventCounters()) {
      min = Math.min(min, eventCounter.getValue());
    }

    return min;
  }
}
