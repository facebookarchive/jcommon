package com.facebook.collections;

import com.google.common.reflect.TypeToken;

public class Key<K, V> {
  private K name;
  private TypeToken<V> type;

  public Key(K id, TypeToken<V> type) {
    this.name = id;
    this.type = type;
  }

  public Key(K id, Class<V> clazz) {
    this.name = id;
    this.type = TypeToken.of(clazz);
  }

  public static <K, V> Key<K, V> get(K id, Class<V> clazz) {
    return new Key<>(id, clazz);
  }

  public static <K, V> Key<K, V> get(K id, TypeToken<V> clazz) {
    return new Key<>(id, clazz);
  }

  public K getName() {
    return name;
  }

  public TypeToken<V> getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Key key = (Key) o;

    if (!type.equals(key.type)) {
      return false;
    }
    if (!name.equals(key.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
