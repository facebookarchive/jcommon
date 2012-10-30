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
