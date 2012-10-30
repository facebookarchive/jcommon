package com.facebook.data.types;

public class DatumFactory {
  public static BooleanDatum toDatum(boolean value) {
    return new BooleanDatum(value);
  }
  
  public static ByteDatum toDatum(byte value) {
    return new ByteDatum(value);
  }

  public static ShortDatum toDatum(short value) {
    return new ShortDatum(value);
  }
  
  public static IntegerDatum toDatum(int value) {
    return new IntegerDatum(value);
  }
  
  public static LongDatum toDatum(long value) {
    return new LongDatum(value);
  }
  
  public static FloatDatum toDatum(float value) {
    return new FloatDatum(value);
  }
  
  public static DoubleDatum toDatum(double value) {
    return new DoubleDatum(value);
  }
  
  public static StringDatum toDatum(String value) {
    return new StringDatum(value);
  }
}
