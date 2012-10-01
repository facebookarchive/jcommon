package com.facebook.collections;

import java.util.Map;

/***
 * marker interface for commonly used type
 *
 * @param <String>
 */
public class StringKeyedMixTypedMap extends MixedTypeMap<String> {
  public StringKeyedMixTypedMap(Map<Key<String, ?>, Object> map) {
    super(map);
  }

  public StringKeyedMixTypedMap() {
  }
}
