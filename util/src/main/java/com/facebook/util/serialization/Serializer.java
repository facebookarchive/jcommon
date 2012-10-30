package com.facebook.util.serialization;


import java.io.DataOutput;

/**
 * interface that takes an instance of T and typically will end up with a stream of bytes being
 * produced, depending on what DataInput is backed by.Implementations are often nested static classes
 * of Class\<T\> in order to have access to private data
 */

public interface Serializer<T> {
  public void serialize(T value, DataOutput out) throws SerDeException;
}
