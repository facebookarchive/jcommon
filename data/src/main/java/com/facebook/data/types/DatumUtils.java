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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * utility methods for operating on Datums
 */
public class DatumUtils {

  /**
   * recursively converts the mapDatum into a valid JSONObject
   *
   * @param mapDatum
   * @return JSONObject
   */
  public static JSONObject buildJSON(MapDatum mapDatum) {
    JSONObject jsonObject = new JSONObject();
    Map<Datum, Datum> map = mapDatum.getMap();

    try {
      for (Map.Entry<Datum, Datum> entry : map.entrySet()) {
        String key = entry.getKey().asString();
        DatumType valueDatumType = entry.getValue().getType();

        if (valueDatumType == DatumType.LIST) {
          jsonObject.put(key, buildJSON((ListDatum) entry.getValue()));
        } else if (valueDatumType == DatumType.MAP) {
          jsonObject.put(key, buildJSON((MapDatum) entry.getValue()));
        } else {
          jsonObject.put(key, entry.getValue().asRaw());
        }
      }

      return jsonObject;
    } catch (JSONException e) {
      throw new RuntimeException("error converting json object to string", e);
    }

  }

  /**
   * recursively converts contained Datums into a valid JSONArray.
   *
   * @param listDatum
   * @return JSONArray
   */
  public static JSONArray buildJSON(ListDatum listDatum) {
    JSONArray jsonArray = new JSONArray();
    List<Datum> datumList = listDatum.asList();

    try {
      int i = 0;

      for (Datum datum : datumList) {
        DatumType datumType = datum.getType();

        if (datumType == DatumType.LIST) {
          jsonArray.put(i, buildJSON((ListDatum) datum));
        } else if (datumType == DatumType.MAP) {
          jsonArray.put(i, buildJSON((MapDatum) datum));
        } else {
          jsonArray.put(i, datum.asRaw());
        }

        i++;
      }

      return jsonArray;
    } catch (JSONException e) {
      throw new RuntimeException(
        "some element did not conform to JSON format: " + datumList,
        e
      );
    }
  }

  /**
   * {impressionTimestamp, adId}
   * <p/>
   * and creates JSON with
   * <pre>
   *
   *  {
   *    keyNames[0] : rawTuple[0],
   *    keyNames[1] : rawTuple[1],
   *    ...
   *    keyNames[n] : rawTuple[n],
   *  }
   *  </pre>
   *
   * @param rawTuple - impressionTimestamp, adId
   * @return
   */
  public static MapDatum toMapDatum(LongDatum[] rawTuple, StringDatum[] keyNames) {
    assert rawTuple.length == keyNames.length;

    Map<Datum, Datum> datumMap = new HashMap<Datum, Datum>(rawTuple.length);

    for (int i = 0; i < rawTuple.length; i++) {
      datumMap.put(keyNames[i], rawTuple[i]);
    }

    return new MapDatum(datumMap);

  }

  /**
   * @param value    byte, short, int, long to translate big-endian to an array
   * @param numBytes 1 = byte, 2 short, 4 = int, 8 = long.  or try 5 for the heck of it.
   * @return
   */
  public static byte[] toBytes(long value, int numBytes) {
    byte[] bytes = new byte[numBytes];

    for (int i = numBytes-1; i > 0; i--) {
      bytes[i] = (byte) value;
      value >>>= 8;
    }

    bytes[0] = (byte) value;

    return bytes;
  }
}
