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

import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.facebook.stats.cardinality.StaticModelUtil.SMALLEST_PROBABILITY;
import static java.lang.Math.E;
import static java.lang.Math.PI;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

@SuppressWarnings({"RedundantArrayCreation", "NonReproducibleMathCall", "ConstantMathCall"})
public class TestArithmeticCodec {
  private static final Random random = new SecureRandom();

  @Test
  public void testPossibleOverflowInClose() throws Exception {
    testRoundTrip(
        new SortedStaticModel(new ExponentiallyDecreasingHistogramFactory().create(32)),
        32,
        Ints.asList(new int[]{25, 22, 1, 11, 5, 3, 6, 5, 25, 9, 2, 9, 3, 3, 17, 20}),
        16
    );
    testRoundTrip(
        new SortedStaticModel(new ExponentiallyDecreasingHistogramFactory().create(32)),
        32,
        Ints.asList(new int[]{27, 28, 1, 25, 4, 5, 15, 7, 14, 3, 23, 15, 25, 12, 3, 15}),
        16
    );
  }

  @Test
  public void testUnderflowBytesInClose() throws Exception {
    testRoundTrip(
        new SortedStaticModel(new ExponentiallyDecreasingHistogramFactory().create(8)),
        8,
        Ints.asList(new int[]{3, 5, 4, 0, 2, 4, 7, 5, 7, 7, 3, 1, 1, 5, 0, 3}),
        16
    );
    testRoundTrip(
        new SortedStaticModel(new ExponentiallyDecreasingHistogramFactory().create(16)),
        16,
        Ints.asList(new int[]{12, 12, 4, 4, 13, 2, 9, 8, 9, 1, 0, 8, 2, 11, 12, 1}),
        16
    );
  }

  @Test
  public void testDecodeZeroPaddingRequired() throws Exception {
    // ArithmeticDecoder buffers 6 bytes; when there are fewer than 6, it should treat the input as
    // if it had zeros for the missing bytes.  In practice, this rarely matters, but for the case
    // below, getting it wrong results in an "IllegalArgumentException: targetCount is negative" due
    // to ArithmeticDecoder.bufferByte() removing underflow bytes from high and low, but not value.
    int[] buckets = new int[2048];

    buckets[860] = 1;
    buckets[1258] = 1;
    buckets[1618] = 1;
    buckets[2033] = 1;

    testRoundTrip(
        HyperLogLogCodec.createHyperLogLogSymbolModel(4, 2048, (byte) 1),
        2,
        Ints.asList(buckets),
        2048
    );
  }

  @Test
  public void testRoundTrip() throws Exception {
    testRoundTrip(new SortedStaticDataModelFactory(new ExponentiallyDecreasingHistogramFactory()));
    testRoundTrip(new SortedStaticDataModelFactory(new GaussianHistogramFactory()));
    testRoundTrip(new SortedStaticDataModelFactory(new RandomHistogramFactory()));

    testRoundTrip(new StaticDataModelFactory(new ExponentiallyDecreasingHistogramFactory()));
    testRoundTrip(new StaticDataModelFactory(new GaussianHistogramFactory()));
    testRoundTrip(new StaticDataModelFactory(new RandomHistogramFactory()));
  }

  public void testRoundTrip(DataModelFactory modelFactory) throws Exception {
    testRoundTrip(modelFactory, new SequentialDataFactory());
    testRoundTrip(modelFactory, new RandomDataFactory());
  }

  private void testRoundTrip(DataModelFactory modelFactory, DataFactory dataFactory)
      throws Exception {
    for (int size = 1; size < 100000; size <<= 1) {
      for (int numberOfSymbols = 2; numberOfSymbols <= 512; numberOfSymbols <<= 1) {
        testRoundTrip(
            modelFactory.create(numberOfSymbols),
            numberOfSymbols,
            dataFactory.create(size, numberOfSymbols),
            size
        );
      }
    }
  }

  private void testRoundTrip(Model model, int numberOfSymbols, Iterable<Integer> symbols, int size)
      throws Exception {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ArithmeticEncoder encoder = new ArithmeticEncoder(model, out);

      for (Integer symbol : symbols) {
        encoder.encode(symbol);
      }
      encoder.close();

      ArithmeticDecoder decoder = new ArithmeticDecoder(model, out.toByteArray());
      for (Integer symbol : symbols) {
        int newData = decoder.decode();

        if (newData != symbol) {
          Assert.assertEquals(
              newData, (int) symbol, String.format(
              "size=%d, numberOfSymbols=%d",
              size,
              numberOfSymbols
          )
          );
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format(
              "size=%d, numberOfSymbols=%d",
              size,
              numberOfSymbols
          ), e
      );
    }
  }

  public static interface DataModelFactory {
    Model create(int numberOfSymbols);
  }

  private static class StaticDataModelFactory implements DataModelFactory {
    private final HistogramFactory histogramFactory;

    private StaticDataModelFactory(HistogramFactory histogramFactory) {
      this.histogramFactory = histogramFactory;
    }

    public StaticModel create(int numberOfSymbols) {
      double[] weights = histogramFactory.create(numberOfSymbols);
      return new StaticModel(weights);
    }
  }

  private static class SortedStaticDataModelFactory implements DataModelFactory {
    private final HistogramFactory histogramFactory;

    private SortedStaticDataModelFactory(HistogramFactory histogramFactory) {
      this.histogramFactory = histogramFactory;
    }

    public SortedStaticModel create(int numberOfSymbols) {
      double[] weights = histogramFactory.create(numberOfSymbols);
      return new SortedStaticModel(weights);
    }
  }

  private interface HistogramFactory {
    public double[] create(int numberOfSymbols);
  }

  private static class ExponentiallyDecreasingHistogramFactory implements HistogramFactory {
    public double[] create(int numberOfSymbols) {
      double maxExponent = log(1 / SMALLEST_PROBABILITY);

      double[] probability = new double[numberOfSymbols];
      for (int symbol = 0; symbol < probability.length; symbol++) {
        double exponent = symbol * maxExponent / numberOfSymbols;
        probability[symbol] = 1.0D / pow(E, exponent);
      }
      return probability;
    }
  }

  private static class GaussianHistogramFactory implements HistogramFactory {
    public double[] create(int numberOfSymbols) {
      double mean = numberOfSymbols / 2.0;
      double std = sqrt(mean);

      double[] probability = new double[numberOfSymbols];
      for (int i = 0; i < probability.length; i++) {
        // see wikipedia
        double value = (1 / (std * sqrt(2.0 * PI))) * (pow(
            E, -(pow(i - mean, 2) / (2 * pow(
            std,
            2
        )))
        ));
        probability[i] = value;
      }
      return probability;
    }
  }

  private static class RandomHistogramFactory implements HistogramFactory {
    public double[] create(int numberOfSymbols) {
      double[] probability = new double[numberOfSymbols];
      for (int i = 0; i < probability.length; i++) {
        double value = random.nextDouble();
        probability[i] = value;
      }
      return probability;
    }
  }

  public static interface DataFactory {
    Iterable<Integer> create(int size, int numberOfSymbols);
  }

  public static class SequentialDataFactory implements DataFactory {
    @Override
    public Iterable<Integer> create(int size, int numberOfSymbols) {
      List<Integer> data = new ArrayList<Integer>(size);
      for (int i = 0; i < size; i++) {
        data.add(i % numberOfSymbols);
      }
      return data;
    }
  }

  public static class RandomDataFactory implements DataFactory {
    @Override
    public Iterable<Integer> create(int size, int numberOfSymbols) {
      List<Integer> data = new ArrayList<Integer>(size);
      for (int i = 0; i < size; i++) {
        data.add(random.nextInt(numberOfSymbols));
      }
      return data;
    }
  }
}
