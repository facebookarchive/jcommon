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
import java.io.IOException;

@SuppressWarnings({"NumericCastThatLosesPrecision"})
public class BooleanDatum implements Datum {
  private final boolean value;

  public BooleanDatum(boolean value) {
    this.value = value;
  }

  @Override
  public boolean asBoolean() {
    return value;
  }

  @Override
  public byte asByte() {
    return (byte) (value ? 1 : 0);
  }

  @Override
  public short asShort() {
    return (short) (value ? 1 : 0);
  }

  @Override
  public int asInteger() {
    return value ? 1 : 0;
  }

  @Override
  public long asLong() {
    return value ? 1 : 0;
  }

  @Override
  public float asFloat() {
    return value ? 1 : 0;
  }

  @Override
  public double asDouble() {
    return value ? 1 : 0;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public byte[] asBytes() {
    return new byte[]{asByte()};
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public DatumType getType() {
    return DatumType.BOOLEAN;
  }

  @Override
  public Object asRaw() {
    return value;
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public int hashCode() {
    return value ? 1 : 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      value == ((Datum) obj).asBoolean();
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return (o.asBoolean() == value ? 0 : (value ? 1 : -1));
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(
      DataInput
        in
    ) throws SerDeException {
      try {
        return new BooleanDatum(in.readBoolean());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeBoolean(value.asBoolean());
      } catch (Exception e) {
        throw new SerDeException(e);
      }
    }
  }
}
