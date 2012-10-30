package com.facebook.data.types;

import com.google.common.base.Preconditions;

/**
 * caches the conversion of a Datum to a long/double. The idea here is to save
 * on cpu cost by caching the conversion to the number format with the highest
 * bit-width
 */
public class NumberCachingDatum extends WrappedDatum {
  private volatile Long longValue;
  private volatile Double doubleValue;
  private volatile boolean checkDone = false;
  private volatile DatumType datumType;

  public NumberCachingDatum(Datum delegate) {
    super(delegate);
  }

  @Override
  public long asLong() {
    return getAsLong();
  }

  @Override
  public boolean asBoolean() {
    return getAsLong() != 0;
  }

  @Override
  public byte asByte() {
    return (byte) getAsLong();
  }

  @Override
  public short asShort() {
    return (short) getAsLong();
  }

  @Override
  public int asInteger() {
    return (int) getAsLong();
  }

  @Override
  public float asFloat() {
    return (float) getAsDouble();
  }

  @Override
  public double asDouble() {
    return getAsDouble();
  }

  @Override
  public DatumType getType() {
    checkType();

    return datumType;
  }

  // does conversion to both long and double
  private void checkType() {
    DatumType type = super.getType();

    switch (type) {
      case NULL:
        datumType = type;
        break;
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case SHORT:
      case INTEGER:
      case LONG:
      case FLOAT:
      case DOUBLE:
        datumType = type;
        break;
      case STRING:
        datumType = computeTypeOfString();
        break;
      case LIST:
      case MAP:
      case OTHER:
      default:
        datumType = type;
        break;
    }
  }

  private DatumType computeTypeOfString() {
    Preconditions.checkState(
      super.getType() == DatumType.STRING,
      "only DatumType of STRING require special processing"
    );

    try {
        getAsLong();

      } catch (NumberFormatException e) {
        // ignore
      }
      try {
        getAsDouble();
      } catch (NumberFormatException e) {
        //ignore
      }

    if (longValue != null && doubleValue != null) {
      // old school way to tell if a double you cast to a long can be represented as a long
      @SuppressWarnings("FloatingPointEquality")
      boolean isLongType = ((double) longValue) == doubleValue;

      return isLongType ? DatumType.LONG : DatumType.DOUBLE;
    } else if (longValue != null) {
      return DatumType.LONG;
    } else if (doubleValue != null) {
      return DatumType.DOUBLE;
    } else {
      return super.getType();
    }
  }


  private double getAsDouble() {
    if (doubleValue == null) {
      doubleValue = super.asDouble();
    }

    return doubleValue;
  }

  private long getAsLong() {
    if (longValue == null) {
      longValue = super.asLong();
    }

    return longValue;
  }
}
