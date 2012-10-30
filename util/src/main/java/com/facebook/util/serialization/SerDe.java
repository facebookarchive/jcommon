package com.facebook.util.serialization;

/**
 * marker interface
 * @param <T>
 */
public interface SerDe<T> extends Serializer<T>, Deserializer<T>{
}
