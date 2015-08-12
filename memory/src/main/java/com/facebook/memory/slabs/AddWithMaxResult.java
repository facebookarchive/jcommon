package com.facebook.memory.slabs;

public class AddWithMaxResult {
  private final long previousValue;
  private final int actualDelta;

  public AddWithMaxResult(long  previousValue, int actualDelta) {
    this.previousValue = previousValue;
    this.actualDelta = actualDelta;
  }

  public long getPreviousValue() {
    return previousValue;
  }

  public int getActualDelta() {
    return actualDelta;
  }
}
