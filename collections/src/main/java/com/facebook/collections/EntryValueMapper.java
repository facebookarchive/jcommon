package com.facebook.collections;

import java.util.Map;

/**
 * maps a Map.Entry<K,V> to V
 * 
 * @param <K> key type of entry
 * @param <V> value type of entry
 */
public class EntryValueMapper<K, V> implements Mapper<Map.Entry<K, V>, V> {
  @Override
  public V map(Map.Entry<K, V> input) {
    return input.getValue();
  }
}
