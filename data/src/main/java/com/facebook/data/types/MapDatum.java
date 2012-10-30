package com.facebook.data.types;

import com.facebook.collectionsbase.Lists;
import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datum is a very generic class that encapsulates actual data as well
 * as methods to inspect what it is.
 */
public class MapDatum implements Datum {
  private static final Comparator<Map.Entry<Datum, Datum>> ENTRY_COMPARATOR =
    new Comparator<Map.Entry<Datum, Datum>>() {
      @Override
      public int compare(
        Map.Entry<Datum, Datum> o1, Map.Entry<Datum, Datum> o2
      ) {
        int keyResult = o1.getKey().compareTo(o2.getKey());

        if (keyResult == 0) {
          return o1.getValue().compareTo(o2.getValue());
        }

        return keyResult;
      }
    };
  private final static DatumSerDe DATUM_SER_DE = new DatumSerDe();

  private final Map<Datum, Datum> map;

  public MapDatum(Map<Datum, Datum> map) {
    this.map = map;
  }

  public MapDatum() {
    this(new HashMap<Datum, Datum>());
  }

  /**
   * @return true if the hash is non-empty
   */
  @Override
  public boolean asBoolean() {
    return !map.isEmpty();
  }

  /**
   * number of keys; may overflow
   *
   * @return
   */
  @Override
  public byte asByte() {
    return (byte) map.size();
  }

  /**
   * number of keys; may overflow
   *
   * @return
   */
  @Override
  public short asShort() {
    return (short) map.size();
  }

  /**
   * number of keys
   *
   * @return
   */
  @Override
  public int asInteger() {
    return map.size();
  }

  /**
   * number of keys
   *
   * @return
   */
  @Override
  public long asLong() {
    return map.size();
  }

  @Override
  public float asFloat() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double asDouble() {
    throw new UnsupportedOperationException();

  }

  /**
   * nested data structure are rendered with asString()
   *
   * @return JSON representation of map { k1 : v1, k2 : v2, ...}
   */
  @Override
  public String asString() {
    JSONObject jsonObject = new JSONObject();

    try {
      for (Map.Entry<Datum, Datum> entry : map.entrySet()) {
        String key = entry.getKey().asString();
        String value = entry.getValue().asString();

        jsonObject.put(key, value);
      }

      return jsonObject.toString(2);
    } catch (JSONException e) {
      throw new RuntimeException("error converting json object to string");
    }
  }

  @Override
  public byte[] asBytes() {
    try {
      // todo: use jackson to to JSON encoding, or can we somehow just
      // call asBytes() on each key/value and concatenate?
      // (also, as this is used for unique counts, who does a unique
      // on Map?  watch a use cometh...)
      return asString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("failed to encode as UTF-8");
    }
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public DatumType getType() {
    return DatumType.MAP;
  }

  @Override
  public Object asRaw() {
    return map;
  }

  public Map<Datum, Datum> getMap() {
    return map;
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
    if (!(o instanceof MapDatum)) {
      return false;
    }

    final MapDatum mapDatum = (MapDatum) o;

    if (map != null ? !map.equals(mapDatum.map) : mapDatum.map != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return map != null ? map.hashCode() : 0;
  }

  @Override
  public int compareTo(Datum o) {
    if (!(o instanceof MapDatum)) {
      throw new IllegalArgumentException("need MapDatum");
    }
    @SuppressWarnings({"unchecked"})
    MapDatum otherMapDatum = (MapDatum) o;
    // this is only used in InMemoryStorage for unit tests, so it can e
    // inefficient; this is terribly inefficient, but it provides
    // some way to comapre maps :)

    List<Map.Entry<Datum, Datum>> entryList1 =
      new ArrayList<>(map.entrySet());
    List<Map.Entry<Datum, Datum>> entryList2 =
      new ArrayList<>(otherMapDatum.map.entrySet());

    Collections.sort(entryList1, ENTRY_COMPARATOR);
    Collections.sort(entryList2, ENTRY_COMPARATOR);

    return Lists.compareLists(entryList1, entryList2, ENTRY_COMPARATOR);
  }

  public static class SerDeImpl implements SerDe<Datum> {
    @Override
    public Datum deserialize(DataInput in) throws SerDeException {
      try {
        int numEntires = in.readInt();
        Map<Datum, Datum> map = new HashMap<>(numEntires);

        for (int i = 0; i < numEntires; i++) {
          Datum key = DATUM_SER_DE.deserialize(in);
          Datum value = DATUM_SER_DE.deserialize(in);

          map.put(key, value);
        }

        MapDatum result = new MapDatum(map);

        return result;
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }

    @Override
    public void serialize(Datum value, DataOutput out)
      throws SerDeException {
      if (value instanceof MapDatum) {
        MapDatum mapDatum = (MapDatum) value;
        Map<Datum, Datum> map = mapDatum.getMap();
        try {
          out.writeInt(map.size());

          for (Map.Entry<Datum, Datum> entry : map.entrySet()) {
            DATUM_SER_DE.serialize(entry.getKey(), out);
            DATUM_SER_DE.serialize(entry.getValue(), out);
          }
        } catch (IOException e) {
          throw new SerDeException(e);
        }
      } else {
        throw new IllegalArgumentException(
          "MapDatum.SerDe serializer requires MapDatum"
        );
      }
    }
  }
}
