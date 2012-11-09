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
public class StringDatum implements Datum {
  private final String value;
  private volatile byte[] bytesValue;

  public StringDatum( String value) {
    if (value == null) {
      throw new NullPointerException("null value not allowed");
    }

    this.value = value;
  }

  @Override
  public boolean asBoolean() {
    boolean check = !"".equals(value) && !"false".equals(value.toLowerCase());

    try {
      check |= asByte() != 0;
    } catch (NumberFormatException e) {
      // a value that isn't a number, but isn't "" or "false" is still 
      // considered true, so ignore this
    }

    return check;
  }

  @Override
  public byte asByte() {
    return Byte.valueOf(value);
  }

  @Override
  public short asShort() {
    return Short.valueOf(value);
  }

  @Override
  public int asInteger() {
    return Integer.valueOf(value);
  }

  @Override
  public long asLong() {
    return Long.valueOf(value);
  }

  @Override
  public float asFloat() {
    return Float.valueOf(value);
  }

  @Override
  public double asDouble() {
    return Double.valueOf(value);
  }

  @Override
  public byte[] asBytes() {
    if (bytesValue == null) {
      bytesValue = value.getBytes();
    }

    return bytesValue;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public DatumType getType() {
    return DatumType.STRING;
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
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      value.equals(((Datum) obj).asString());
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return value.compareTo(o.asString());
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        // TODO: should we write the bytes ourselves?  get around 32k limit?
        return new StringDatum(in.readUTF());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeUTF(value.asString());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
