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

import com.google.common.base.Preconditions;

import java.util.Arrays;

final class StaticModelUtil {
  public static final int COUNT_BITS = 12;
  public static final int MAX_COUNT = 1 << COUNT_BITS;

  // this is a little bigger than 1.0 / MAX_COUNT
  public static final double SMALLEST_PROBABILITY = 2.5e-4;

  private StaticModelUtil() {
  }

  public static double[] weightsToProbabilities(double[] weights) {
    return weightsToProbabilities(weights, Integer.MAX_VALUE);
  }

  public static double[] weightsToProbabilities(double[] weights, int iterationLimit) {
    Preconditions.checkNotNull(weights, "weights is null");
    Preconditions.checkArgument(weights.length > 0, "weights is empty");

    return weightsToProbabilities(
      Arrays.copyOf(weights, weights.length), sum(weights), iterationLimit
    );
  }

  private static strictfp double[] weightsToProbabilities(
    double[] weights, double sum, int iterationLimit
  ) {
    int iterationCount = 0;

    do {
      for (int i = 0; i < weights.length; i++) {
        Preconditions.checkArgument(
            weights[i] >= 0,
            String.format("weight %s value %s is not greater than zero", i, weights[i])
        );
        weights[i] /= sum;

        // adjust probability is too small or too large
        // this number is large enough that when multiplied by MAX_TOTAL we get a whole number
        if (weights[i] < SMALLEST_PROBABILITY) {
          weights[i] = SMALLEST_PROBABILITY;
        } else if (weights[i] > 0.999) {
          // this generally leaves enough room for ever symbol to get a whole number part of MAX_TOTAL
          weights[i] = 0.999;
        }
      }

      // keep normalizing until the total probability falls in a specific range
      sum = sum(weights);
      ++iterationCount;
    } while ((sum < 0.9999 || sum > 1.0001) && iterationCount < iterationLimit);

    return weights;
  }

  private static strictfp double sum(double[] values) {
    double sum = 0;
    for (double value : values) {
      sum += value;
    }
    return sum;
  }
}
