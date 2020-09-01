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

import static com.facebook.stats.cardinality.StaticModelUtil.weightsToProbabilities;

import com.google.common.base.Preconditions;
import java.util.Arrays;

class StaticModel implements Model {
  private final int[] counts;

  private final int totalIndex;

  public StaticModel(double[] weights) {
    Preconditions.checkNotNull(weights, "weights is null");

    Preconditions.checkArgument(weights.length > 1, "weights is empty");
    Preconditions.checkArgument(
        weights.length <= 512, "weights is can not have more than 512 entries");

    counts = new int[weights.length + 1];
    totalIndex = weights.length;

    double[] probabilities = weightsToProbabilities(weights, 10);

    for (int symbol = 0; symbol < probabilities.length; symbol++) {
      double probability = probabilities[symbol];

      // value is low count + % of MAX_TOTAL
      int value = counts[symbol] + ((int) (StaticModelUtil.MAX_COUNT * probability));
      // reserve one space for each symbol
      value = Math.min(value, StaticModelUtil.MAX_COUNT - (probabilities.length - symbol));
      // high must be at least one bigger than the low
      value = Math.max(value, counts[symbol] + 1);

      counts[symbol + 1] = value;
      if (counts[symbol + 1] <= counts[symbol]) {
        Preconditions.checkState(
            counts[symbol + 1] > counts[symbol],
            "Internal error: symbol %s high value %s is not greater than the low value %s",
            symbol,
            counts[symbol + 1],
            counts[symbol]);
      }
    }

    Preconditions.checkState(
        counts[totalIndex - 1] < StaticModelUtil.MAX_COUNT,
        "Internal error: model max value %s must be less than %s");
    counts[totalIndex] = StaticModelUtil.MAX_COUNT;

    // verify model
    for (int i = 1; i < counts.length; i++) {
      Preconditions.checkState(counts[i - 1] < counts[i], "Internal error: model is invalid");
    }
  }

  @Override
  public SymbolInfo getSymbolInfo(int symbol) {
    Preconditions.checkPositionIndex(symbol, counts.length, "symbol");
    return new SymbolInfo(symbol, counts[symbol], counts[symbol + 1]);
  }

  @Override
  public int log2MaxCount() {
    return StaticModelUtil.COUNT_BITS;
  }

  @Override
  public SymbolInfo countToSymbol(int targetCount) {
    Preconditions.checkArgument(targetCount >= 0, "targetCount is negative %s", targetCount);

    // binary search for value
    int lowSymbol = 0;
    int highSymbol = counts.length;
    while (highSymbol > lowSymbol + 1) {
      int midSymbol = (lowSymbol + highSymbol) >>> 1;
      if (counts[midSymbol] > targetCount) {
        // value is smaller
        highSymbol = midSymbol;
      } else {
        // value is larger or equal
        lowSymbol = midSymbol;
      }
    }
    return new SymbolInfo(lowSymbol, counts[lowSymbol], counts[lowSymbol + 1]);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StaticModel staticModel = (StaticModel) o;

    if (totalIndex != staticModel.totalIndex) {
      return false;
    }
    if (!Arrays.equals(counts, staticModel.counts)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(counts);
    result = 31 * result + totalIndex;
    return result;
  }
}
