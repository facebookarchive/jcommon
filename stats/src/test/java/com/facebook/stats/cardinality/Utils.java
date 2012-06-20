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
