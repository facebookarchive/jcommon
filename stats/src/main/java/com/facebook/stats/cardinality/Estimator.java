package com.facebook.stats.cardinality;

interface Estimator {
  boolean setIfGreater(int bucket, int highestBitPosition);
  long estimate();
  int estimateSizeInBytes();
  int[] buckets();
  int getNumberOfBuckets();
  int getMaxAllowedBucketValue();
}
