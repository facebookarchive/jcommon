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

import java.util.Map;
import java.util.concurrent.Callable;

import com.facebook.stats.MultiWindowDistribution;
import com.facebook.stats.MultiWindowRate;
import com.facebook.stats.MultiWindowSpread;

public interface StatsReader {
  public void exportCounters(Map<String, Long> counters);
  public MultiWindowRate getRate(String key);
  public MultiWindowRate getRate(StatType statType);
  public MultiWindowRate getSum(String key);
  public MultiWindowRate getSum(StatType statType);
  public long getCounter(StatType key);
  public long getCounter(String key);
  public MultiWindowSpread getSpread(StatType key);
  public MultiWindowSpread getSpread(String key);
  public MultiWindowDistribution getDistribution(StatType key);
  public MultiWindowDistribution getDistribution(String key);
  public String getAttribute(StatType key);
  public String getAttribute(String key);
  @Deprecated
  public Callable<Long> getDynamicCounter(StatType key);
  @Deprecated
  public Callable<Long> getDynamicCounter(String key);

  /**
   * @return returns a snapshot copy of the attributes
   */
  public Map<String, String> getAttributes();
}

