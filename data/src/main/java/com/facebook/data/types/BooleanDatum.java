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
