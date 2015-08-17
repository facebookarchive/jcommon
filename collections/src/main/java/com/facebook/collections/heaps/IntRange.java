package com.facebook.collections.heaps;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.util.Comparator;

@Immutable
public class IntRange implements Comparable<IntRange> {
  private static final Comparator<IntRange> SIZE_COMPARATOR = (o1, o2) -> {
    int sigNum = Integer.signum(o1.getSize() - o2.getSize());

    // break size ties by location
    if (sigNum == 0) {
      return Integer.signum(o1.getLower() - o2.getLower());
    } else {
      return sigNum;
    }
  };
  private final int lower;
  private final int upper;

  /**
   * creates a set [lower, upper]
   *
   * @param lower
   * @param upper
   */
  public IntRange(int lower, int upper) {
    Preconditions.checkArgument(lower >= 0, "%d < 0, not allowed", lower);
    Preconditions.checkArgument(lower <= upper, "invalid range [%d, %d]", lower, upper);
    this.lower = lower;
    this.upper = upper;
  }

  public static IntRange make(int lower, int upper) {
    return new IntRange(lower, upper);
  }

  public static IntRange emptyRange(int location) {
    return new EmptyRange(location);
  }


  public static Comparator<IntRange> getSizeComparator() {
    return SIZE_COMPARATOR;
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

  /**
   * returns a new range with offset removed from the LHS
   * [s, e] -> [s + offset, 100]
   * [0, 100], offset=10 -> [10, 100]
   *
   * @param offset
   * @return
   */

  public IntRange shave(int offset) {
    Preconditions.checkArgument(offset <= getSize());

    if (offset == getSize()) {
      return IntRange.emptyRange(lower);
    } else {
      return new IntRange(lower + offset, upper);
    }
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

  private static class EmptyRange extends IntRange {
    EmptyRange(int location) {
      super(location, location);
    }

    @Override
    public int getSize() {
      return 0;
    }

    @Override
    public IntRange shave(int offset) {
      return this;
    }

    @Override
    public boolean contains(int x) {
      return false;
    }
  }
}
