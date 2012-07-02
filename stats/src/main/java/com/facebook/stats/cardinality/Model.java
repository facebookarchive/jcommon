/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.stats.cardinality;

interface Model {
  SymbolInfo getSymbolInfo(int symbol);

  SymbolInfo countToSymbol(int count);

  /**
   * The lg(MAX_COUNT) of the model.
   * <p/>
   * The arithmetic coding implementation uses shifting instead of
   * multiplication and division, because shifting is significantly
   * faster.  For this to work, the max count must be a power of 2,
   * and this method gives the coder shift size.
   */
  int log2MaxCount();

  public static final class SymbolInfo {
    private final int symbol;
    private final int lowCount;
    private final int highCount;

    public SymbolInfo(int symbol, int lowCount, int highCount) {
      this.symbol = symbol;
      this.lowCount = lowCount;
      this.highCount = highCount;
    }

    public int symbol() {
      return symbol;
    }

    public int lowCount() {
      return lowCount;
    }

    public int highCount() {
      return highCount;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("SymbolInfo");
      sb.append("{symbol=").append(symbol);
      sb.append(", lowCount=").append(lowCount);
      sb.append(", highCount=").append(highCount);
      sb.append('}');
      return sb.toString();
    }
  }
}
