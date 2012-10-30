package com.facebook.util.serialization;


import java.io.DataInput;

/**
 * interface that takes a stream of bytes wrapped in the DataInput interface (to make
 * reading primitives easier), and builds a T. Implementations are often nested static classes
 * of Class\<T\> in order to have access to specialized, private constructors
 *
 * @param <T>
 */
public interface Deserializer<T> {
  public T deserialize(DataInput in) throws SerDeException;
}
