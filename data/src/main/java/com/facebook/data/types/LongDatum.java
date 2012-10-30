package com.facebook.data.types;


import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings({"NumericCastThatLosesPrecision"})
public class LongDatum implements Datum {
  private final long value;
  private volatile byte[] bytes;

  public LongDatum(long value) {
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
  public byte[] asBytes() {
    if (bytes == null) {
      bytes = DatumUtils.toBytes(value, 8);
    }

    return bytes;
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
    return DatumType.LONG;
  }

  @Override
  public Object asRaw() {
    return value;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(value).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Datum &&
      value == ((Datum) obj).asLong();
  }

  @Override
  public int compareTo(Datum o) {
    if (o == null) {
      return 1;
    }

    return Long.signum(value - o.asLong());
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        return new LongDatum(in.readLong());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      try {
        out.writeLong(value.asLong());
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
