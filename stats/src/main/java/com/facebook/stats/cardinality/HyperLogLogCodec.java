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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Output format is:
 *
 * <pre>
 *     estimate - 4 byte float
 *     log2(buckets) - unsigned nibble
 *     log2Ceiling(maxValue) - unsigned nibble
 *     bucket values - arithmetic encoded
 * </pre>
 */
public class HyperLogLogCodec {
  public void encodeHyperLogLog(HyperLogLog hyperLogLog, OutputStream out) throws IOException {
    Preconditions.checkNotNull(hyperLogLog, "hyperLogLog is null");

    encodeHyperLogLog(hyperLogLog, new DataOutputStream(out));
  }

  public void encodeHyperLogLog(HyperLogLog hyperLogLog, DataOutputStream out) throws IOException {
    encodeBuckets(out, hyperLogLog.buckets(), hyperLogLog.estimate());
  }

  public void encodeAdaptiveHyperLogLog(AdaptiveHyperLogLog hyperLogLog, OutputStream out)
      throws IOException {
    Preconditions.checkNotNull(hyperLogLog, "hyperLogLog is null");

    encodeAdaptiveHyperLogLog(hyperLogLog, new DataOutputStream(out));
  }

  public void encodeAdaptiveHyperLogLog(AdaptiveHyperLogLog hyperLogLog, DataOutputStream out)
      throws IOException {
    encodeBuckets(out, hyperLogLog.buckets(), hyperLogLog.estimate());
  }

  public void encodeBuckets(DataOutputStream out, int[] buckets, float estimate)
      throws IOException {
    // get the number of buckets, which must be a power of 2
    int numberOfBuckets = buckets.length;
    Preconditions.checkArgument(numberOfBuckets > 0, "buckets is empty");
    Preconditions.checkArgument(
        isPowerOf2(numberOfBuckets), "numberOfBuckets must be a power of 2");

    // find the max value
    int maxValue = buckets[0];
    for (int value : buckets) {
      if (value > maxValue) {
        maxValue = value;
      }
    }
    // replace max value with the next power of 2, so we can encode using log2
    maxValue = nextPowerOf2(maxValue);

    // write the estimate as a float
    int estimateFloatBits = Float.floatToIntBits(estimate);
    out.writeInt(estimateFloatBits);
    // "read" the estimate back from the float bits because for large values we will loose
    // precision, also the JVM will use 40 bit floats on some platforms and we will not have
    // this extra precision when the value is deseralized
    estimate = Float.intBitsToFloat(estimateFloatBits);

    // write the bucket count and max value in as two nibbles
    int log2Buckets = Integer.numberOfTrailingZeros(numberOfBuckets);
    // max value is the max number of symbols used during encoding
    int log2MaxValue = Integer.numberOfTrailingZeros(maxValue);
    byte bucketsAndMaxValue = (byte) (log2Buckets << 4 | log2MaxValue);
    out.write(bucketsAndMaxValue);

    // create the static symbol model based on the estimated cardinality
    SortedStaticModel hyperLogLogModel =
        createHyperLogLogSymbolModel((long) estimate, numberOfBuckets, (byte) maxValue);

    // create the encoder with an estimated output size
    ArithmeticEncoder encoder = new ArithmeticEncoder(hyperLogLogModel, out);

    // write each byte
    for (int symbol : buckets) {
      encoder.encode(symbol);
    }

    // finalize the encoding
    encoder.close();
  }

  public HyperLogLog decodeHyperLogLog(InputStream in) throws IOException {
    return decodeHyperLogLog(new DataInputStream(in));
  }

  public HyperLogLog decodeHyperLogLog(DataInputStream in) throws IOException {
    int[] buckets = decodeBuckets(in);
    return new HyperLogLog(buckets);
  }

  public AdaptiveHyperLogLog decodeAdaptiveHyperLogLog(InputStream in) throws IOException {
    return decodeAdaptiveHyperLogLog(new DataInputStream(in));
  }

  public AdaptiveHyperLogLog decodeAdaptiveHyperLogLog(DataInputStream in) throws IOException {
    int[] buckets = decodeBuckets(in);
    return new AdaptiveHyperLogLog(buckets);
  }

  private int[] decodeBuckets(DataInputStream in) throws IOException {
    Preconditions.checkNotNull(in, "in is null");

    // read the estimate
    int estimateFloatBits = in.readInt();
    float estimate = Float.intBitsToFloat(estimateFloatBits);

    // read the bucket count and max value which were encoded as two nibbles
    byte bucketsAndMaxValue = in.readByte();
    int log2NumberOfBuckets = (bucketsAndMaxValue >> 4) & 0xF;
    int numberOfBuckets = 1 << log2NumberOfBuckets;
    int log2MaxValue = bucketsAndMaxValue & 0xF;
    byte maxValue = (byte) (1 << log2MaxValue);

    // create the model
    SortedStaticModel hyperLogLogModel =
        createHyperLogLogSymbolModel((long) estimate, numberOfBuckets, maxValue);

    // read the bucket values
    int[] buckets = new int[numberOfBuckets];
    ArithmeticDecoder decoder = new ArithmeticDecoder(hyperLogLogModel, in);
    for (int index = 0; index < buckets.length; index++) {
      buckets[index] = decoder.decode();
    }
    return buckets;
  }

  public static SortedStaticModel createHyperLogLogSymbolModel(
      long estimate, int bucketCount, byte maxValue) {
    double[] probability = hyperLogLogProbabilities(estimate, bucketCount, maxValue);
    return new SortedStaticModel(probability);
  }

  public static double[] hyperLogLogProbabilities(
      long cardinality, int bucketCount, byte maxValue) {
    // the probability of each symbol
    double[] probabilities = new double[(int) maxValue + 1];

    double lastCumulativeProbability = 0;
    for (int value = 0; value < (int) maxValue; value++) {
      double cumulativeProbability = probabilityRegisterLessThan(cardinality, bucketCount, value);
      probabilities[value] = cumulativeProbability - lastCumulativeProbability;
      lastCumulativeProbability = cumulativeProbability;
    }

    return probabilities;
  }

  public static double probabilityRegisterLessThan(long cardinality, int bucketCount, int value) {
    return StrictMath.pow(1.0d - 1.0d / ((1 << value) * 1.0d * bucketCount), cardinality);
  }

  private static byte nextPowerOf2(int value) {
    // Integer.highestOneBit returns the input value with all bits cleared
    // except for the highest 1 bit (this is very confusing).
    int newValue = Integer.highestOneBit(value);

    // if the newValue == value, value was a power of two
    if (newValue < value) {
      // otherwise, shift the new value over one bit
      newValue <<= 1;
    }
    // if original value was 0, make it 1
    if (newValue == 0) {
      newValue++;
    }
    return (byte) newValue;
  }

  private static boolean isPowerOf2(int numberOfBuckets) {
    return (numberOfBuckets & (numberOfBuckets - 1)) == 0;
  }
}
