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

import com.facebook.collectionsbase.Lists;
import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a list of Datums. Retrieve the list via asRaw();
 * <p/>
 * of the list, but this might lead to confusing issues. Ways to get the List\<Datum\>
 *   <pre>
 *     // preferred, though more verbose
 *     void fuu(Datum someDatum) {
 *       if (someDatum.getType() == DatumType.LIST) {
 *         ListDatum listDatum = (ListDatum)someDatum;
 *         List<Datum> datumList = listDatum.asList();
 *       }
 *     }
 *
 *     // more concise, but asRaw() thus far has a loose contract and is only
  *     void bar(Datum someDatum) {
  *       if (someDatum.getType() == DatumType.LIST) {
  *         List<Datum> datumList = (List<Datum>)someDatum.asRaw();
  *       }
  *     }
 *
 *
 *   </pre>
 *
 */
public class ListDatum implements Datum {
  private static final int STRING_DATUM_SIZE_ESTIMATE = 8;
  private static final char DEFAULT_SEPARATOR = '\001';

  private final List<Datum> datumList;
  private final char separator;
  private final Datum scalarDatum;
  private volatile String cachedStringDatum = null;
  private volatile byte[] cachedBytes = null;

  public ListDatum(
    List<Datum> datumList, char separator
  ) {
    this.datumList = datumList;
    this.separator = separator;

    if (datumList.size() == 1) {
      scalarDatum = datumList.get(0);
    } else {
      scalarDatum = null;
    }
  }

  public ListDatum(List<Datum> datumList) {
    this(datumList, DEFAULT_SEPARATOR);
  }

  char getSeparator() {
    return separator;
  }

  List<Datum> getDatumList() {
    return datumList;
  }

  @Override
  public boolean asBoolean() {
    return !datumList.isEmpty();
  }

  @Override
  public byte asByte() {
    if (scalarDatum != null) {
      return scalarDatum.asByte();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public short asShort() {
    if (scalarDatum != null) {
      return scalarDatum.asShort();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public int asInteger() {
    if (datumList.size() == 1) {
      return scalarDatum.asInteger();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public long asLong() {
    if (scalarDatum != null) {
      return scalarDatum.asLong();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public float asFloat() {
    if (scalarDatum != null) {
      return scalarDatum.asFloat();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public double asDouble() {
    if (scalarDatum != null) {
      return scalarDatum.asDouble();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public byte[] asBytes() {
    if (cachedBytes == null) {
      try {
        cachedBytes = asString().getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        // this shouldn't happen, but it's fatal of it does
        throw new RuntimeException(e);
      }
    }

    return cachedBytes;
  }

  @Override
  public String asString() {
    if (scalarDatum != null) {
      cachedStringDatum = scalarDatum.asString();
    } else if (cachedStringDatum == null) {
      StringBuilder sb = new StringBuilder(
        datumList.size() * STRING_DATUM_SIZE_ESTIMATE
      );

      for (Datum datum : datumList) {
        if (sb.length() > 0) {
          sb.append(separator);
        }
        sb.append(datum.asString());
      }

      cachedStringDatum = sb.toString();
    }

    return cachedStringDatum;
  }

  @Override
  public boolean isNull() {
    // TODO: is [NulLDatum] == null ?  i say no as does this impl
    return false;
  }

  @Override
  public DatumType getType() {
    return DatumType.LIST;
  }

  @Override
  public Object asRaw() {
    return datumList;
  }

  /**
   * same as asRaw(), but just saves you from having to  do
   * <pre> {@code
   *  List<Datum> list = (List<Datum>)((ListDatum)datum).asRaw()
   *  } </pre>
   * becomes
   * <pre> {@code
   *  List<Datum> list = ((ListDatum).datum).asList()
   *  } </pre>
   *
   * @return
   */
  public List<Datum> asList() {
    return datumList;
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListDatum)) {
      return false;
    }

    final ListDatum listDatum = (ListDatum) o;

    if (datumList != null ? !datumList.equals(listDatum.datumList) : listDatum.datumList != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = datumList != null ? datumList.hashCode() : 0;
    result = 31 * result + (scalarDatum != null ? scalarDatum.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(Datum o) {
    if (o instanceof ListDatum) {
      ListDatum otherListDatum = (ListDatum) o;

      return Lists.compareLists(datumList, otherListDatum.datumList);
    } else {
      throw new IllegalArgumentException(
        String.format(
          "ListDatum cannot compare to %s", o.getClass()
        )
      );
    }
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        int numItems = in.readInt();
        List<Datum> datumList = new ArrayList<Datum>(numItems);

        for (int i = 0; i < numItems; i++) {
          byte typeAsByte = in.readByte();
          DatumType datumType = DatumType.fromByte(typeAsByte);
          Datum datum = datumType.getSerDe().deserialize(in);

          datumList.add(datum);
        }

        ListDatum listDatum = new ListDatum(datumList);

        return listDatum;
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      if (value instanceof ListDatum) {
        try {
          ListDatum listDatum = (ListDatum) value;

          out.writeInt(listDatum.datumList.size());

          for (Datum datum : listDatum.datumList) {
            DatumType datumType = datum.getType();
            SerDe<Datum> datumSerDe = datumType.getSerDe();
            // write type
            out.writeByte(datumType.getTypeAsByte());
            // now the datm
            datumSerDe.serialize(datum, out);
          }


        } catch (IOException e) {
          throw new SerDeException(e);
        }

      } else {
        throw new IllegalArgumentException(
          "ListDatum.SerDeImpl requires ListDatum, not " +
            value.getClass()
        );
      }
    }
  }
}
