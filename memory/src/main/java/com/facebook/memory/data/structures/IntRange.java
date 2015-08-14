package com.facebook.memory.data.structures;

import com.google.common.base.Preconditions;

public class IntRange implements Comparable<IntRange>{
  private final int lower;
  private final int upper;

  public IntRange(int lower, int upper) {
    Preconditions.checkArgument(lower >= 0, "%d < 0, not allowed", lower);
    Preconditions.checkArgument(lower <= upper, "invalid range [%d, %d]", lower, upper);
    this.lower = lower;
    this.upper = upper;
  }

  public int getLower() {
    return lower;
  }

  public int getUpper() {
    return upper;
  }

  public int getSize() {
    return upper - lower + 1;
  }

  public IntRange shave(int offset) {
    Preconditions.checkArgument(offset < getSize());

    return new IntRange(lower + offset, upper);
  }

  public boolean contains(int x) {
    return lower <= x && x <= upper;
  }

  public boolean overlaps(IntRange range) {
    return range != null && (contains(range.lower) || contains(range.upper));
  }

  public boolean isAdjacentTo(IntRange range) {
    return range != null && (upper + 1 == range.lower || range.upper + 1 == lower);
  }

  public IntRange span(IntRange range) {
    Preconditions.checkNotNull(range);

    return new IntRange(Math.min(lower, range.lower), Math.max(upper, range.upper));
  }

  public IntRange extend(int size) {
    return new IntRange(lower, upper + size);
  }

  @Override
  public int compareTo(IntRange range) {
    Preconditions.checkNotNull(range);
    return Integer.signum(lower - range.lower);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntRange)) {
      return false;
    }

    IntRange intRange = (IntRange) o;

    if (lower != intRange.lower) {
      return false;
    }
    if (upper != intRange.upper) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = lower;
    result = 31 * result + upper;
    return result;
  }

  @Override
  public String toString() {
    return "IntRange{" +
      "lower=" + lower +
      ", upper=" + upper +
      ", size=" + getSize() +
      '}';
  }
}
