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
