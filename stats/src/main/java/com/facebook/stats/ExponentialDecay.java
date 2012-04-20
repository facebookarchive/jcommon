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
