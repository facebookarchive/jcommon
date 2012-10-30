package com.facebook.data.types;

import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings({"NumericCastThatLosesPrecision"})
public class DoubleDatum implements Datum {
  private final double value;

  public DoubleDatum(double value) {
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
    return (float) value;
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
    return DatumType.DOUBLE;
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
    return Double.valueOf(value).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      Double.compare(value, ((Datum) obj).asDouble()) == 0;
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return Double.compare(value, o.asFloat());
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        return new DoubleDatum(in.readDouble());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeDouble(value.asDouble());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
