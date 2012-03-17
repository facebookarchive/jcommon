package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public interface CompositeEventCounterIf<C extends EventCounterIf<C>> 
  extends EventCounterIf<C> {
  
  public CompositeEventCounterIf<C> add(
    long delta, ReadableDateTime start, ReadableDateTime end
  );  
  public CompositeEventCounterIf<C> addEventCounter(C eventCounter);  
}
