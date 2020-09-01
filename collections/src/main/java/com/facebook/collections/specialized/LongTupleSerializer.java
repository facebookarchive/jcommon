/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.collections.specialized;

import com.facebook.util.serialization.SerDeException;
import com.facebook.util.serialization.Serializer;
import java.io.DataOutput;
import java.io.IOException;

/**
 * base serializer for all long[](TimestampedLongTuple) types
 *
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
