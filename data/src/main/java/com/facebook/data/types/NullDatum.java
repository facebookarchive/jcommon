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
package com.facebook.data.types;



import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;

public class NullDatum implements Datum {
  // convenient instance to use
  public static final NullDatum INSTANCE = new NullDatum();

  private NullDatum() {
  }

  @Override
  public boolean asBoolean() {
    return false;
  }

  @Override
  public byte asByte() {
    return 0;
  }

  @Override
  public short asShort() {
    return 0;
  }

  @Override
  public int asInteger() {
    return 0;
  }

  @Override
  public long asLong() {
    return 0;
  }

  @Override
  public float asFloat() {
    return 0;
  }

  @Override
  public double asDouble() {
    return 0;
  }

  @Override
  public byte[] asBytes() {
    return null;
  }

  @Override
  public String asString() {
    return "null";
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public DatumType getType() {
    return DatumType.NULL;
  }

  @Override
  public Object asRaw() {
    return null;
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof NullDatum;
  }

  @Override
  public int compareTo(Datum o) {
    if (o instanceof NullDatum) {
      return 0;
    } else {
      return -1;
    }
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      return NullDatum.INSTANCE;
    }

    @Override
    public void serialize(Datum value, DataOutput out) throws SerDeException {
    }
  }
}
