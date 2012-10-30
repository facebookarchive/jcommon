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
