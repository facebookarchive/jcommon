package com.facebook.collections;

public class Key<K, V> {
  private K name;
  private Class<V> clazz;

  public Key(K id, Class<V> clazz) {
    this.name = id;
    this.clazz = clazz;
  }

  public static <K, V> Key<K, V> get(K id, Class<V> clazz) {
    return new Key<K, V>(id, clazz);
  }

  public K getName() {
    return name;
  }

  public Class<V> getClazz() {
    return clazz;
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

    if (!clazz.equals(key.clazz)) {
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
    result = 31 * result + clazz.hashCode();
    return result;
  }
}
