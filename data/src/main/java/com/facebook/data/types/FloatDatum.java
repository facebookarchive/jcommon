package com.facebook.data.types;


import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings({"NumericCastThatLosesPrecision"})
public class FloatDatum implements Datum {
  private final float value;

  public FloatDatum( float value) {
    this.value = value;
  }

  @Override
  public boolean asBoolean() {
    return value != 0;
  }

  @Override
  public byte asByte() {
    return (byte) value;
  }

  @Override
  public short asShort() {
    return (short) value;
  }

  @Override
  public int asInteger() {
    return (int) value;
  }

  @Override
  public long asLong() {
    return (long) value;
  }

  @Override
  public float asFloat() {
    return value;
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public byte[] asBytes() {
    // TODO:
    throw new UnsupportedOperationException();
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
    return DatumType.FLOAT;
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
    return Float.valueOf(value).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      Float.compare(value, ((Datum) obj).asFloat()) == 0;
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return Float.compare(value, o.asFloat());
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        return new FloatDatum(in.readFloat());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeFloat(value.asFloat());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
