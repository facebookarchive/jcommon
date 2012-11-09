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
package com.facebook.stats;

import org.joda.time.ReadableDateTime;

public interface CompositeEventCounterIf<C extends EventCounterIf<C>> extends EventCounterIf<C> {
  /**
   * creates a new counter and adds it to the composite one. Despite being a public method, this is
   * most often used inside of other counters to add a new "head" counter when computing multiple
   * window lengths. The counter object is shared by all windows and eventually merged when its
   * duration has completed
   *
   * @param delta value of the new counter
   * @param start
   * @param end
   * @return the CompositeEventCounterIf after adding the new counter
   */
  public CompositeEventCounterIf<C> add(
    long delta, ReadableDateTime start, ReadableDateTime end
  );

  /**
   * Add an already created counter to the ComopsiteEvenCountIf. This is similar to
   * {@link #add(long, org.joda.time.ReadableDateTime, org.joda.time.ReadableDateTime)} but
   * externalizes creation of the counter to add.  ie, you can add another CompositeEventCounterIf
   * if C so allows it, allowing for nesting of composition to arbitrary depths.
   *
   * @param eventCounter counter to add
   * @return this after adding eventCounter
   */
  public CompositeEventCounterIf<C> addEventCounter(C eventCounter);
}
