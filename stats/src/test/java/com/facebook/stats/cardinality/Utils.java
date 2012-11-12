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
package com.facebook.stats.cardinality;

public class Utils {
  public static int entropy(int[] histogram) {
    int total = 0;
    for (int value : histogram) {
      total += value;
    }

    double sum = 0;
    for (int k : histogram) {
      if (k > 0) {
        double p = k * 1.0 / total;
        sum += p * Math.log(p);
      }
    }

    return (int) Math.ceil(-total * sum / Math.log(2));
  }

  public static int[] histogram(int[] values) {
    int[] frequencies = new int[255];
    for (int value : values) {
      frequencies[value]++;
    }

    return frequencies;
  }
}
