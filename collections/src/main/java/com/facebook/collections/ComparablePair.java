package com.facebook.collections;

public class ComparablePair
  <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>> 
  extends Pair<T1, T2> implements Comparable<ComparablePair<T1, T2>>{
  
  public ComparablePair(T1 t1, T2 t2) {
    super(t1, t2);
  }

  @Override
  public int compareTo(ComparablePair<T1, T2> o) {
    int firstCompareTo = getFirst().compareTo(o.getFirst());

    if (firstCompareTo == 0) {
      return getSecond().compareTo(o.getSecond());
    }
    
    return firstCompareTo;
  }
  
  // using Pair's equals()/hashCode() 
}