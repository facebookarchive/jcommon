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

import java.util.List;

/**
 * general type of a Datum; this can help a caller determine the type
 * of the asRaw() method and is used for serializing Datums
 */
public enum DatumType {
  NULL(new NullDatum.SerDeImpl(), -1),
  BOOLEAN(new BooleanDatum.SerDeImpl(), 0),
  BYTE(new ByteDatum.SerDeImpl(), 1),
  CHAR(new ByteDatum.SerDeImpl(), 2), // TOOD: not used yet
  SHORT(new ShortDatum.SerDeImpl(), 3),
  INTEGER(new IntegerDatum.SerDeImpl(), 4),
  LONG(new LongDatum.SerDeImpl(), 5),
  FLOAT(new FloatDatum.SerDeImpl(), 6),
  DOUBLE(new DoubleDatum.SerDeImpl(), 7),
  STRING(new StringDatum.SerDeImpl(), 8),
  LIST(new ListDatum.SerDeImpl(), 9),
  MAP(new MapDatum.SerDeImpl(), 10),
  OTHER(null, 127), // TODO ?
  ;

  private final SerDe<Datum> serDe;
  private final byte typeAsByte;

  DatumType(SerDe<Datum> serDe, byte typeAsByte) {
    this.serDe = serDe;
    this.typeAsByte = typeAsByte;
  }

  DatumType(SerDe<Datum> serDe, int i) {
    this(serDe, (byte) i);
  }

  public SerDe<Datum> getSerDe() {
    return serDe;
  }

  public byte getTypeAsByte() {
    return typeAsByte;
  }

  public static DatumType fromByte(byte typeAsByte) {
    if (typeAsByte == NULL.getTypeAsByte()) {
      return NULL;
    } else if (typeAsByte == BOOLEAN.getTypeAsByte()) {
      return BOOLEAN;
    } else if (typeAsByte == BYTE.getTypeAsByte()) {
      return BYTE;
    } else if (typeAsByte == CHAR.getTypeAsByte()) {
      return CHAR;
    } else if (typeAsByte == SHORT.getTypeAsByte()) {
      return SHORT;
    } else if (typeAsByte == INTEGER.getTypeAsByte()) {
      return INTEGER;
    } else if (typeAsByte == LONG.getTypeAsByte()) {
      return LONG;
    } else if (typeAsByte == FLOAT.getTypeAsByte()) {
      return FLOAT;
    } else if (typeAsByte == DOUBLE.getTypeAsByte()) {
      return DOUBLE;
    } else if (typeAsByte == STRING.getTypeAsByte()) {
      return STRING;
    } else if (typeAsByte == LIST.getTypeAsByte()) {
      return LIST;
    } else if (typeAsByte == MAP.getTypeAsByte()) {
      return MAP;
    } else if (typeAsByte == OTHER.getTypeAsByte()) {
      return OTHER;
    } else {
      throw new IllegalArgumentException("unknown byte type: " + typeAsByte);
    }
  }

  /**
   * helper function to see if the value is a Long or less
   *
   * @param datum
   * @return
   */
  public static boolean isLongCompatible(Datum datum) {
    DatumType datumType = datum.getType();
    boolean singleItemType = datumType.compareTo(BOOLEAN) >= 0 && datumType.compareTo(LONG) <= 0;

    if (!singleItemType && datumType == LIST) {
      // only special case is List, since we promote a single-item list to a "scalar" datum
      // recursively 
      List<Datum> listDatum = ((ListDatum) datum).asList();

      return listDatum.size() == 1 && isLongCompatible(listDatum.get(0));
    } else {
      return singleItemType;
    }
  }
}
