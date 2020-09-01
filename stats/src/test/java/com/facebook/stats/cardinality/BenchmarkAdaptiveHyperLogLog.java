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

import static java.lang.String.format;

import com.google.common.base.Throwables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;

public class BenchmarkAdaptiveHyperLogLog {
  private static final int COMPRESSION_LOOPS = 10000;
  private static final int COMPRESSION_WARM_LOOPS = 1000;

  public static void main(String[] args) {
    System.out.println("Warming up...");
    System.out.println();

    benchmark(1024, (1L << 20), false);

    System.out.println("Benchmarking...");
    System.out.println();

    long count = (1L << 30);
    benchmark(1024, count, true);
    benchmark(2048, count, true);
    benchmark(4096, count, true);
  }

  private static void benchmark(int buckets, long count, boolean report) {
    if (report) {
      System.out.println(
          format("-- %s buckets (%.2f%% error)", buckets, 100 * 1.04 / Math.sqrt(buckets)));
      System.out.println();
      System.out.println(
          "                   |        adaptive      |         fixed        | delta fixed vs adapt.|       size (bytes)       |                    |     serialization    ");
      System.out.println(
          "            actual |    estimate  error % |    estimate  error % |       count  error % | actual  entropy     mean | ns/add       add/s | bytes  enc ms  dec ms");
    }

    HyperLogLog fixedEstimator = new HyperLogLog(buckets);
    AdaptiveHyperLogLog adaptiveEstimator = new AdaptiveHyperLogLog(buckets);

    HyperLogLogCodec codec = new HyperLogLogCodec();

    Random random = new Random();
    long reportInterval = 1;
    long nanos = 0;
    for (long i = 1; i <= count; ++i) {
      long value = random.nextLong();
      long start = System.nanoTime();
      adaptiveEstimator.add(value);
      nanos += System.nanoTime() - start;

      fixedEstimator.add(value);

      if (report && i % reportInterval == 0 || i % 5000000 == 0) {
        long adaptiveEstimate = adaptiveEstimator.estimate();
        double adaptiveError = (adaptiveEstimate - i) * 100.0 / i;

        long fixedEstimate = fixedEstimator.estimate();
        double fixedError = (fixedEstimate - i) * 100.0 / i;

        int encodeSize = encodeSize(codec, adaptiveEstimator);
        double encodeMs = timeEncode(codec, adaptiveEstimator);
        double decodeMs = timeDecode(codec, adaptiveEstimator);

        System.out.print(
            format(
                "\r(%3d%%) %11d | %11d  %7.2f | %11d  %7.2f | %11d  %7.2f | %6d  %7d  %7.2f | %6d  %10.2f | %5d  %5.4f  %5.4f",
                i * 100 / count,
                i,
                adaptiveEstimate,
                adaptiveError,
                fixedEstimate,
                fixedError,
                adaptiveEstimate - fixedEstimate,
                Math.abs(adaptiveError) - Math.abs(fixedError),
                adaptiveEstimator.getSizeInBytes(),
                Utils.entropy(Utils.histogram(adaptiveEstimator.buckets())) / 8,
                adaptiveEstimator.getSizeInBytes() * 1.0 / i,
                nanos / i,
                i / (nanos / 1.0e9),
                encodeSize,
                encodeMs,
                decodeMs));

        if (report && i % reportInterval == 0) {
          System.out.println();
          reportInterval *= 2;
        }
      }
    }

    if (report) {
      System.out.println();
      System.out.println();
    }
  }

  private static int encodeSize(HyperLogLogCodec codec, AdaptiveHyperLogLog hyperLogLog) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(hyperLogLog.buckets().length);
      codec.encodeAdaptiveHyperLogLog(hyperLogLog, out);
      byte[] buf = out.toByteArray();
      return buf.length;
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private static double timeEncode(HyperLogLogCodec codec, AdaptiveHyperLogLog hyperLogLog) {
    try {
      int buckets = hyperLogLog.buckets().length;

      long encodeTime = 0;
      for (int i = 0; i < COMPRESSION_LOOPS; i++) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(buckets);

        long startTime = System.nanoTime();
        codec.encodeAdaptiveHyperLogLog(hyperLogLog, out);
        long delta = System.nanoTime() - startTime;

        if (i > COMPRESSION_WARM_LOOPS) {
          encodeTime += delta;
        }
      }
      return encodeTime / 1.0e6 / (COMPRESSION_LOOPS - COMPRESSION_WARM_LOOPS);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private static double timeDecode(HyperLogLogCodec codec, AdaptiveHyperLogLog hyperLogLog) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(hyperLogLog.buckets().length);
      codec.encodeAdaptiveHyperLogLog(hyperLogLog, out);
      byte[] buf = out.toByteArray();

      long decodeTime = 0;
      for (int i = 0; i < COMPRESSION_LOOPS; i++) {

        long startTime = System.nanoTime();
        codec.decodeAdaptiveHyperLogLog(new DataInputStream(new ByteArrayInputStream(buf)));
        long delta = System.nanoTime() - startTime;

        if (i > COMPRESSION_WARM_LOOPS) {
          decodeTime += delta;
        }
      }
      return decodeTime / 1.0e6 / (COMPRESSION_LOOPS - COMPRESSION_WARM_LOOPS);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
