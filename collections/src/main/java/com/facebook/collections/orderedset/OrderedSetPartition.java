package com.facebook.collections.orderedset;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.NavigableSet;

public class OrderedSetPartition<T extends Comparable<? super T>> implements Comparable<OrderedSetPartition<T>> {
  private final NavigableSet<T> set;
  private final T upperBound;
  private final Comparator<? super T> lowerBoundComparator;

  public OrderedSetPartition(NavigableSet<T> set, T upperBound, Comparator<? super T> lowerBoundComparator) {
    this.set = set;
    this.upperBound = upperBound;
    this.lowerBoundComparator = lowerBoundComparator;
  }

  public OrderedSetPartition(@Nullable NavigableSet<T> set, T upperBound) {
    this(set, upperBound, (o1, o2) -> o1.compareTo(o2));
  }

  public static <K extends Comparable<? super K>> OrderedSetPartition<K> keyOnlyShard(K key) {
    return new OrderedSetPartition<>(null, key);
  }

  public static <K extends Comparable<? super K>>
  Comparator<OrderedSetPartition<K>> comparatorFrom(Comparator<K> boundaryComparator) {
    return (o1, o2) -> boundaryComparator.compare(o1.getUpperBound(), o2.getUpperBound());
  }

  public NavigableSet<T> getSet() {
    return set;
  }

  public T getUpperBound() {
    return upperBound;
  }

  @Override
  public int compareTo(OrderedSetPartition<T> o) {
    return lowerBoundComparator.compare(upperBound, o.upperBound);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    OrderedSetPartition<?> orderedSetPartition = (OrderedSetPartition<?>) o;
    return Objects.equal(upperBound, orderedSetPartition.upperBound);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(upperBound);
  }
}
