/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.stats.cardinality;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class TestHyperLogLogCodecSpeed {
  private static final int LOOPS = 10000;
  private static final int WARM_LOOPS = 1000;

  @Test(groups = "slow", enabled = false)
  public void testHyperLogLog()
      throws Exception {
    warm();

    testHyperLogLog(10);
    testHyperLogLog(11);
    testHyperLogLog(12);
  }

  public void testHyperLogLog(int log2m)
      throws Exception {
    System.out.printf(
        "%11s  %11s  %6s  %4s  %4s  %4s  %9s  %6s  %6s\n",
        "actual",
        "estimate",
        "error",
        "in",
        "out",
        "ent",
        "bits/Byte",
        "enc ms",
        "dec ms"
    );

    HyperLogLog hyperLogLog = new HyperLogLog(1 << log2m);

    long currentSize = 0;
    for (long size = 1; size < 200000000; size <<= 1) {
      for (; currentSize < size; currentSize++) {
        hyperLogLog.add(currentSize);
      }
      long estimate = hyperLogLog.estimate();
      double err = Math.abs(estimate - size) / (double) size;

      int entropy = Utils.entropy(Utils.histogram(hyperLogLog.buckets())) / 8;

      testBytes(hyperLogLog, log2m, size, estimate, err, entropy);
    }

    System.out.println();
  }

  public static void testBytes(
      HyperLogLog hyperLogLog,
      int log2m,
      long size,
      long estimate,
      double err,
      int entropy
  ) throws IOException {
    int buckets = 1 << log2m;
    HyperLogLogCodec codec = new HyperLogLogCodec();

    // encode
    ByteArrayOutputStream out = new ByteArrayOutputStream(buckets);
    codec.encodeHyperLogLog(hyperLogLog, out);
    byte[] compressed = out.toByteArray();

    // decode
    HyperLogLog hyperLogLogNew = codec.decodeHyperLogLog(new ByteArrayInputStream(compressed));

    // verify results
    assertEquals(hyperLogLog.buckets(), hyperLogLogNew.buckets());

    // time encode and decode
    double encodeMs = timeEncode(codec, hyperLogLog);
    double decodeMs = timeDecode(codec, compressed);

    // print info
    double bitsPerByte = 1000.0 * compressed.length * 8.0 / (double) buckets / 1000.0;
    System.out.printf(
        "%11d  %11d  %5.4f  %4d  %4d  %4d     %5.4f  %5.4f  %5.4f\n",
        size,
        estimate,
        err,
        buckets,
        compressed.length,
        entropy,
        bitsPerByte,
        encodeMs,
        decodeMs
    );
  }

  private static void warm() throws IOException {
    int bucketCount = 1 << 11;
    HyperLogLog hyperLogLog = new HyperLogLog(bucketCount);
    for (long i = 0; i < 100000; i++) {
      hyperLogLog.add(i);
    }

    HyperLogLogCodec codec = new HyperLogLogCodec();

    double encodeMs = timeEncode(codec, hyperLogLog);
    System.out.printf("encode %5.4f\n", encodeMs);

    ByteArrayOutputStream out = new ByteArrayOutputStream(bucketCount);
    codec.encodeHyperLogLog(hyperLogLog, out);
    byte[] buf = out.toByteArray();

    double decodeMs = timeDecode(codec, buf);

    System.out.printf("decode %5.4f\n", decodeMs);
  }

  private static double timeEncode(HyperLogLogCodec codec, HyperLogLog hyperLogLog)
      throws IOException {
    int buckets = hyperLogLog.buckets().length;

    long encodeTime = 0;
    for (int i = 0; i < LOOPS; i++) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(buckets);

      long startTime = System.nanoTime();
      codec.encodeHyperLogLog(hyperLogLog, out);
      long delta = System.nanoTime() - startTime;

      if (i > WARM_LOOPS) {
        encodeTime += delta;
      }
    }
    return encodeTime / 1.0e6 / (LOOPS - WARM_LOOPS);
  }

  private static double timeDecode(HyperLogLogCodec codec, byte[] buf) throws IOException {
    long decodeTime = 0;
    for (int i = 0; i < LOOPS; i++) {

      long startTime = System.nanoTime();
      codec.decodeHyperLogLog(new DataInputStream(new ByteArrayInputStream(buf)));
      long delta = System.nanoTime() - startTime;

      if (i > WARM_LOOPS) {
        decodeTime += delta;
      }
    }
    return decodeTime / 1.0e6 / (LOOPS - WARM_LOOPS);
  }
}
