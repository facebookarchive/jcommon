package com.facebook.stats.cardinality;

import com.google.common.base.Preconditions;

import java.util.Random;

public class HyperLogLogUtil {
  public static long estimateCardinality(int[] bucketValues) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(bucketValues.length),
      "number of buckets must be a power of 2"
    );
    int bits = Integer.numberOfTrailingZeros(bucketValues.length); // log2(bucketValues.length)

    double alpha;
    switch (bits) {
      case 4:
        alpha = 0.673;
        break;
      case 5:
        alpha = 0.697;
        break;
      case 6:
        alpha = 0.709;
        break;
      default:
        alpha = 0.7213 / (1 + 1.079 / bucketValues.length);
    }

    int zeroBuckets = 0;
    double sum = 0;
    for (Integer value : bucketValues) {
      sum += 1.0 / (1L << value);
      if (value == 0) {
        ++zeroBuckets;
      }
    }

    double result = alpha * bucketValues.length * bucketValues.length / sum;

    if (result <= 2.5 * bucketValues.length) {
      // adjust for small cardinalities
      if (zeroBuckets > 0) {
        // baselineCount is the number of buckets with value 0
        result = bucketValues.length * Math.log(bucketValues.length * 1.0 / zeroBuckets);
      }
    }

    return Math.round(result);
  }

  public static int[] generateBuckets(int numberOfBuckets, long cardinality) {
    Preconditions.checkArgument(
      Numbers.isPowerOf2(numberOfBuckets),
      "number of buckets must be a power of 2"
    );

    double[] probabilities = computeProbabilities(numberOfBuckets, cardinality, Byte.MAX_VALUE);

    Random random = new Random();
    int[] result = new int[numberOfBuckets];
    for (int i = 0; i < numberOfBuckets; ++i) {
      double trial = random.nextDouble();

      int k = 0;
      while (trial > probabilities[k]) {
        ++k;
      }

      result[i] = k;
    }

    return result;
  }

  /**
   * Probability that a bucket has a value <= k
   */
  private static double cumulativeProbability(int numberOfBuckets, long cardinality, int k) {
    return Math.pow(1.0 - 1.0 / ((1L << k) * 1.0 * numberOfBuckets), cardinality);
  }

  /**
   * Compute cumulative probabilities for value <= k for all k = 0..maxK
   */
  private static double[] computeProbabilities(int numberOfBuckets, long cardinality, int maxK) {
    double[] result = new double[maxK];

    for (int k = 0; k < maxK; ++k) {
      result[k] = cumulativeProbability(numberOfBuckets, cardinality, k);
    }

    return result;
  }
}
