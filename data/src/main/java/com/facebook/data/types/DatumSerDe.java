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
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * top-level DatumSerDe container class that handles reading/writing the root of 
 * all Datum serialization structures.
 */
public class DatumSerDe implements SerDe<Datum> {
  @Override
  public Datum deserialize(DataInput in) throws SerDeException {
    try {
      DatumType datumType = DatumType.fromByte(in.readByte());
      SerDe<Datum> serDe = datumType.getSerDe();
      
      // invariant: the SerDe for any Datum will NOT read its own type
      return serDe.deserialize(in);
    } catch (IOException e) {
      throw new SerDeException(e);
    }
  }

  @Override
  public void serialize(Datum value, DataOutput out) throws SerDeException {
    DatumType datumType = value.getType();
    SerDe<Datum> serDe = datumType.getSerDe();

    try {
      // invariant: the SerDe for any Datum will NOT write its own type
      out.writeByte(datumType.getTypeAsByte());
      serDe.serialize(value, out);
    } catch (IOException e) {
      throw new SerDeException(e);
    }
  }
}
