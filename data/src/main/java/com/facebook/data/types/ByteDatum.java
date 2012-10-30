package com.facebook.data.types;


import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings({"NumericCastThatLosesPrecision"})
public class ByteDatum implements Datum {
  private final byte value;

  public ByteDatum(byte value) {
    this.value = value;
  }

  @Override
  public boolean asBoolean() {
    return value != 0;
  }

  @Override
  public byte asByte() {
    return value;
  }

  @Override
  public short asShort() {
    return value;
  }

  @Override
  public int asInteger() {
    return value;
  }

  @Override
  public long asLong() {
    return value;
  }

  @Override
  public float asFloat() {
    return (float) value;
  }

  @Override
  public double asDouble() {
    return (double) value;
  }

  @Override
  public String asString() {
    return String.valueOf(value);
  }

  @Override
  public byte[] asBytes() {
    return new byte[]{value};
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public DatumType getType() {
    return DatumType.BYTE;
  }

  @Override
  public Object asRaw() {
    return value;
  }

  @Override
  public int hashCode() {
    return Byte.valueOf(value).hashCode();
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      value == ((Datum) obj).asByte();
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return Integer.signum(value - o.asByte());
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        return new ByteDatum(in.readByte());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeByte(value.asByte());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
