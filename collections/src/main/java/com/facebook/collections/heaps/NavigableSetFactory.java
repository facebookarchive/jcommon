package com.facebook.collections.heaps;

import java.util.NavigableSet;

import com.facebook.collections.SetFactory;

public interface NavigableSetFactory<T extends Comparable<? super T>> extends SetFactory<T, NavigableSet<T>> {
}
