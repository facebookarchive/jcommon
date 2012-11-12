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

import static com.google.common.base.Preconditions.checkArgument;

public final class ExponentialDecay {
  private ExponentialDecay() {
  }

  /**
   * Compute the alpha decay factor such that the weight of an entry with age 'targetAgeInSeconds'
   * is targetWeight'
   */
  public static double computeAlpha(double targetWeight, int targetAgeInSeconds) {
    checkArgument(targetAgeInSeconds > 0, "targetAgeInSeconds must be > 0");
    checkArgument(targetWeight > 0 && targetWeight < 1, "targetWeight must be in range (0, 1)");

    return -Math.log(targetWeight) / targetAgeInSeconds;
  }

}
