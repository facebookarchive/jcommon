package com.facebook.stats.cardinality;

import java.util.Random;

import static java.lang.String.format;

public class BenchmarkAdaptiveHyperLogLog {
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
        format(
          "-- %s buckets (%.2f%% error)", buckets, 100 * 1.04 / Math.sqrt(
          buckets
        )
        )
      );
      System.out.println();
      System.out.println(
        "                   |        adaptive      |         fixed        | delta fixed vs adapt.|       size (bytes)       |                   "
      );
      System.out.println(
        "            actual |    estimate  error % |    estimate  error % |       count  error % | actual  entropy     mean | ns/add       add/s"
      );
    }

    HyperLogLog fixedEstimator = new HyperLogLog(buckets);
    AdaptiveHyperLogLog adaptiveEstimator = new AdaptiveHyperLogLog(buckets);

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

        System.out.print(
          format(
            "\r(%3d%%) %11d | %11d  %7.2f | %11d  %7.2f | %11d  %7.2f | %6d  %7d  %7.2f | %6d  %10.2f",
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
            i / (nanos / 1e9)
          )
        );

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

}
