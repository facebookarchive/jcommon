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
  /**
   * @param type
   * @param delta
   */
  public void incrementRate(StatType type, long delta);

  /**
   * @param key
   * @param delta
   */
  public void incrementRate(String key, long delta);

  /**
   * @param type
   * @param delta
   */
  public void incrementSum(StatType type, long delta);

  /**
   * @param key
   * @param delta
   */
  public void incrementSum(String key, long delta);

  /**
   * @param key
   * @param delta
   */
  public void incrementCounter(StatType key, long delta);

  /**
   * @param key
   * @param delta
   */
  public void incrementCounter(String key, long delta);

  /**
   * deprecated, see
   *
   * see {@link StatsUtil#setCounterValue(String, long, Stats)}
   */
  @Deprecated
  public long setCounter(String key, long value);

  /**
   * deprecated, see
   *
   * see {@link StatsUtil#setCounterValue(String, long, Stats)}
   */
  @Deprecated
  public long setCounter(StatType key, long value);

  /**
   * resets the counter to 0 in serial executions. In concurrent executions, if increment operations
   * overlap with this call, the value may not be 0 afterwards
   *
   * @param key
   * @return value when reset took effect
   */
  public long resetCounter(StatType key);

  /**
   * see {@link #resetCounter(StatType)}
   *
   * @param key
   * @param delta
   * @return value when reset took effect
   */
  public long resetCounter(String key);

  /**
   * @param type
   * @param value
   */
  public void incrementSpread(StatType type, long value);

  /**
   * @param key
   * @param value
   */
  public void incrementSpread(String key, long value);

  /**
   * @param type
   * @param value
   */
  public void updateDistribution(StatType type, long value);

  /**
   * @param key
   * @param value
   */
  public void updateDistribution(String key, long value);

  /**
   * @param key
   * @param value
   */
  public void setAttribute(StatType key, String value);

  /**
   * @param key
   * @param value
   */
  public void setAttribute(String key, String value);

  /**
   * @param key
   * @param valueProducer
   */
  public void setAttribute(StatType key, Callable<String> valueProducer);

  /**
   * @param key
   * @param valueProducer
   */
  public void setAttribute(String key, Callable<String> valueProducer);
}

