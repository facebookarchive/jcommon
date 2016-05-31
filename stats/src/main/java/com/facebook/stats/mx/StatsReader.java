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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.facebook.stats.MultiWindowDistribution;
import com.facebook.stats.MultiWindowRate;
import com.facebook.stats.MultiWindowSpread;

interface StatsReader {
  void exportCounters(Map<String, Long> counters);
  MultiWindowRate getRate(String key);
  MultiWindowRate getRate(StatType statType);
  MultiWindowRate getSum(String key);
  MultiWindowRate getSum(StatType statType);
  long getCounter(StatType key);
  long getCounter(String key);
  MultiWindowSpread getSpread(StatType key);
  MultiWindowSpread getSpread(String key);
  MultiWindowDistribution getDistribution(StatType key);
  MultiWindowDistribution getDistribution(String key);
  String getAttribute(StatType key);
  String getAttribute(String key);
  @Deprecated
  Callable<Long> getDynamicCounter(StatType key);
  @Deprecated
  Callable<Long> getDynamicCounter(String key);

  /**
   * @return returns a snapshot copy of the attributes
   */
  Map<String, String> getAttributes();

  default Map<String, Long> getCounters() {
    Map<String, Long> result = new HashMap<>();
    exportCounters(result);

    return result;
  }

}

