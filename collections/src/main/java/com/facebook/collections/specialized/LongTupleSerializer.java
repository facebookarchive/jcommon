package com.facebook.collections.specialized;

import com.facebook.util.serialization.SerDeException;
import com.facebook.util.serialization.Serializer;

import java.io.DataOutput;
import java.io.IOException;

/**
 * base serializer for all long[](TimestampedLongTuple) types
 * @param <T>
 */
public class LongTupleSerializer implements Serializer<long[]> {
  @Override
  public void serialize(long[] value, DataOutput out) throws SerDeException {
    try {
      // don't write length--assumption is SerDe knows what it's writing
      for (long item : value) {
        out.writeLong(item);
      }

    } catch (IOException e) {
      throw new SerDeException(e);
    }
  }
}
