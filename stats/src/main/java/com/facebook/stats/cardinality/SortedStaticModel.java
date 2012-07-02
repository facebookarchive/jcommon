package com.facebook.stats.cardinality;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.facebook.stats.cardinality.StaticModelUtil.weightsToProbabilities;

class SortedStaticModel implements Model {
  private final int[] symbolToIndex;
  private final int[] indexToSymbol;

  private final int[] countsByIndex;

  private final int totalIndex;

  public SortedStaticModel(double[] weights) {
    Preconditions.checkNotNull(weights, "weights is null");

    Preconditions.checkArgument(weights.length > 1, "weights is empty");
    Preconditions.checkArgument(
        weights.length <= 512,
        "weights is can not have more than 512 entries"
    );

    symbolToIndex = new int[weights.length + 1];
    indexToSymbol = new int[weights.length + 1];
    countsByIndex = new int[weights.length + 1];
    totalIndex = weights.length;

    double[] probabilities = weightsToProbabilities(weights);
    List<SymbolProbability> symbolProbabilities = sortProbabilities(probabilities);

    int symbolIndex = 0;
    for (SymbolProbability symbolProbability : symbolProbabilities) {
      int symbol = symbolProbability.symbol;
      double probability = symbolProbability.probability;

      symbolToIndex[symbol] = symbolIndex;
      indexToSymbol[symbolIndex] = symbol;

      // value is low count + % of MAX_TOTAL
      int value = countsByIndex[symbolIndex] + ((int) (StaticModelUtil.MAX_COUNT * probability));
      // reserve one space for each symbol
      value = Math.min(value, StaticModelUtil.MAX_COUNT - (probabilities.length - symbolIndex));
      // high must be at least one bigger than the low
      value = Math.max(value, countsByIndex[symbolIndex] + 1);

      countsByIndex[symbolIndex + 1] = value;
      if (countsByIndex[symbolIndex + 1] <= countsByIndex[symbolIndex]) {
        Preconditions.checkState(
            countsByIndex[symbolIndex + 1] > countsByIndex[symbolIndex],
            "Internal error: symbol %s high value %s is not greater than the low value %s",
            symbol,
            countsByIndex[symbolIndex + 1],
            countsByIndex[symbolIndex]
        );
      }

      symbolIndex++;
    }

    Preconditions.checkState(
        countsByIndex[totalIndex - 1] < StaticModelUtil.MAX_COUNT,
        "Internal error: model max value %s must be less than %s"
    );
    symbolToIndex[totalIndex] = -1;
    countsByIndex[totalIndex] = StaticModelUtil.MAX_COUNT;

    // verify model
    for (int i = 1; i < countsByIndex.length; i++) {
      Preconditions.checkState(
          countsByIndex[i - 1] < countsByIndex[i],
          "Internal error: model is invalid"
      );
    }
  }

  @Override
  public SymbolInfo getSymbolInfo(int symbol) {
    Preconditions.checkPositionIndex(symbol, symbolToIndex.length, "symbol");
    int symbolIndex = symbolToIndex[symbol];
    return new SymbolInfo(symbol, countsByIndex[symbolIndex], countsByIndex[symbolIndex + 1]);
  }

  @Override
  public int log2MaxCount() {
    return StaticModelUtil.COUNT_BITS;
  }

  @Override
  public SymbolInfo countToSymbol(int targetCount) {
    Preconditions.checkArgument(targetCount >= 0, "targetCount is negative %s", targetCount);

    // Since symbols are sorted by probability, simply linearly search for the symbol
    for (int symbolIndex = 0; symbolIndex < countsByIndex.length; symbolIndex++) {
      int count = countsByIndex[symbolIndex + 1];
      if (targetCount < count) {
        return new SymbolInfo(
            indexToSymbol[symbolIndex],
            countsByIndex[symbolIndex],
            countsByIndex[symbolIndex + 1]
        );
      }
    }
    throw new IllegalArgumentException("invalid target count " + targetCount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SortedStaticModel staticModel = (SortedStaticModel) o;

    if (totalIndex != staticModel.totalIndex) {
      return false;
    }
    if (!Arrays.equals(countsByIndex, staticModel.countsByIndex)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(countsByIndex);
    result = 31 * result + totalIndex;
    return result;
  }

  private static class SymbolProbability implements Comparable<SymbolProbability> {
    private final int symbol;
    private final double probability;

    private SymbolProbability(int symbol, double probability) {
      this.symbol = symbol;
      this.probability = probability;
    }

    @Override
    public int compareTo(SymbolProbability o) {
      return ComparisonChain
          .start()
          .compare(o.probability, probability)
          .compare(symbol, o.symbol)
          .result();
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("SymbolProbability");
      sb.append("{symbol=").append(symbol);
      sb.append(", probability=").append(probability);
      sb.append('}');
      return sb.toString();
    }
  }

  private List<SymbolProbability> sortProbabilities(double[] probabilities) {
    ArrayList<SymbolProbability> symbolProbabilities = new ArrayList<SymbolProbability>();
    for (int symbol = 0; symbol < probabilities.length; symbol++) {
      symbolProbabilities.add(new SymbolProbability(symbol, probabilities[symbol]));
    }
    Collections.sort(symbolProbabilities);
    return symbolProbabilities;
  }
}
