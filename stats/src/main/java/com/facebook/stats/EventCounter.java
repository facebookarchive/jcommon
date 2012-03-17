package com.facebook.stats;

/**
 * marker interface:  this is required for for any class that requires a generic
 * of the type C extends EventCounterIf<C>
 */
public interface EventCounter extends EventCounterIf<EventCounter>{
}
