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
package com.facebook.stats.mx;

import java.util.concurrent.Callable;

public interface StatsCollector {
  public void incrementRate(StatType type, long delta);
  public void incrementRate(String key, long delta);
  public void incrementCounter(StatType key, long delta);
  public void incrementCounter(String key, long delta);
  public void incrementSpread(StatType type, long value);
  public void incrementSpread(String key, long value);
  public void updateDistribution(StatType type, long value);
  public void updateDistribution(String key, long value);
  public void setAttribute(StatType key, String value);
  public void setAttribute(String key, String value);
  public void setAttribute(StatType key, Callable<String> valueProducer);
  public void setAttribute(String key, Callable<String> valueProducer);
}

