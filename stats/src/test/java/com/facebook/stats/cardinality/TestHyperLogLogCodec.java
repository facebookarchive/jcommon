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

import com.google.common.io.Closeables;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.assertEquals;

public class TestHyperLogLogCodec {
  @Test
  public void testHyperLogLogRoundtrip() throws IOException {
    testHyperLogLogRoundtrip(1024);
    testHyperLogLogRoundtrip(2048);
    testHyperLogLogRoundtrip(4096);
  }

  private void testHyperLogLogRoundtrip(int buckets) throws IOException {
    HyperLogLog expected = new HyperLogLog(buckets);
    for (int i = 0; i < 30000; ++i) {
      expected.add(i);
    }

    HyperLogLogCodec codec = new HyperLogLogCodec();

    // encode
    ByteArrayOutputStream out = new ByteArrayOutputStream(buckets);
    codec.encodeHyperLogLog(expected, out);
    byte[] compressed = out.toByteArray();

    // decode
    HyperLogLog actual = codec.decodeHyperLogLog(new ByteArrayInputStream(compressed));

    // verify results
    assertEquals(actual.buckets(), expected.buckets());
    assertEquals(actual.estimate(), expected.estimate());
  }

  @Test
  public void testAdaptiveHyperLogLogRoundtripLowCardinality() throws IOException {
    testAdaptiveHyperLogLogRoundtrip(1024, 10);
    testAdaptiveHyperLogLogRoundtrip(2048, 10);
    testAdaptiveHyperLogLogRoundtrip(4096, 10);
  }

  @Test
  public void testAdaptiveHyperLogLogRoundtripHighCardinality() throws IOException {
    testAdaptiveHyperLogLogRoundtrip(1024, 30000);
    testAdaptiveHyperLogLogRoundtrip(2048, 30000);
    testAdaptiveHyperLogLogRoundtrip(4096, 30000);
  }

  private void testAdaptiveHyperLogLogRoundtrip(int buckets, int cardinality) throws IOException {
    AdaptiveHyperLogLog expected = new AdaptiveHyperLogLog(buckets);
    for (int i = 0; i < cardinality; ++i) {
      expected.add(i);
    }

    HyperLogLogCodec codec = new HyperLogLogCodec();

    // encode
    ByteArrayOutputStream out = new ByteArrayOutputStream(buckets);
    codec.encodeAdaptiveHyperLogLog(expected, out);
    byte[] compressed = out.toByteArray();

    // decode
    AdaptiveHyperLogLog actual = codec.decodeAdaptiveHyperLogLog(new ByteArrayInputStream(compressed));

    // verify results
    assertEquals(actual.buckets(), expected.buckets());
    assertEquals(actual.estimate(), expected.estimate());
  }

  @Test
  public void testDeserializationBackwardsCompatibility() throws Exception {
    HyperLogLogCodec codec = new HyperLogLogCodec();
    for (int cardinality = 10; cardinality <= 100000; cardinality *= 10) {
      for (int bucketCount = 1024; bucketCount <= 4096; bucketCount <<= 1) {
        String fileBaseName = String.format(
            "serialization/HyperLogLog-%d-%d",
            bucketCount,
            cardinality
        );

        // read the raw bucket values
        AdaptiveHyperLogLog expected;
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileBaseName + ".raw");
        try {
          int[] buckets = new int[bucketCount];
          for (int i = 0; i < bucketCount; i++) {
            buckets[i] = in.read();
          }
          expected = new AdaptiveHyperLogLog(buckets);
        } finally {
          Closeables.close(in, true);
        }

        // write the serialized data
        AdaptiveHyperLogLog actual;
        in = getClass().getClassLoader().getResourceAsStream(fileBaseName + ".ser");
        try {
          actual = codec.decodeAdaptiveHyperLogLog(in);
        } finally {
          Closeables.close(in, true);
        }

        // verify results
        assertEquals(actual.buckets(), expected.buckets());
        assertEquals(actual.estimate(), expected.estimate());
      }
    }
  }

  /**
   * Generate new serialized HyperLogLog files for backwards compatibility test.
   */
  public static void main(String[] args) throws Exception {
    File directory = new File("src/test/resources/serialization");
    directory.mkdirs();

    HyperLogLogCodec codec = new HyperLogLogCodec();
    for (int cardinality = 10; cardinality <= 100000; cardinality *= 10) {
      for (int bucketCounts = 1024; bucketCounts <= 4096; bucketCounts <<= 1) {
        AdaptiveHyperLogLog hyperLogLog = new AdaptiveHyperLogLog(bucketCounts);
        for (int i = 0; i < cardinality; ++i) {
          hyperLogLog.add(i);
        }

        String fileBaseName = String.format("HyperLogLog-%d-%d", bucketCounts, cardinality);

        // write the serialized data
        OutputStream out = new FileOutputStream(new File(directory, fileBaseName + ".ser"));
        try {
          codec.encodeAdaptiveHyperLogLog(hyperLogLog, out);
        } finally {
          Closeables.close(out, true);
        }

        // write the raw bucket values
        out = new FileOutputStream(new File(directory, fileBaseName + ".raw"));
        try {
          for (int bucketValue : hyperLogLog.buckets()) {
            out.write(bucketValue);
          }
        } finally {
          Closeables.close(out, true);
        }
      }
    }
  }
}
