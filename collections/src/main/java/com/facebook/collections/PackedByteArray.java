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
package com.facebook.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackedByteArray {
  private static final byte DEFAULT_DELIMITER = 1;
  private static final byte DEFAULT_TERMINAL_DELIMITER = 0;
  private static final int MAGIC_INITIAL_BYTE_ARRAY_SIZE = 256;

  /**
   * @param arrays
   * @return
   * @see #pack(java.util.List)
   */
  public static byte[] pack(byte[]... arrays) {
    return pack(Arrays.asList(arrays));
  }

  /**
   * reads from a DataInput a byte[] till a delimiter is encountered. Return the bytes (not
   * including the delimiter)
   *
   * @param in
   * @param terminalDelimiter
   * @return
   * @throws IOException
   */
  public static byte[] readByteArray(DataInput in, byte terminalDelimiter) throws IOException {

    // 256 magic number--just guessing it won't be bigger. If it is,
    // ArrayList will resize
    List<Byte> byteList = new ArrayList<Byte>(MAGIC_INITIAL_BYTE_ARRAY_SIZE);
    byte b;

    while ((b = in.readByte()) != terminalDelimiter) {
      byteList.add(b);
    }

    return byteListToArray(byteList);
  }

  /**
   * reads from a DataInput a List<byte[]>
   *
   * @param in
   * @param delimiter
   * @param terminalDelimiter
   * @return
   * @throws IOException
   * @see #packComparable(java.util.List, byte, byte) for format
   */
  public static List<byte[]> readByteArrayList(DataInput in, byte delimiter, byte terminalDelimiter)
      throws IOException {

    // 256 magic number--just guessing it won't be bigger. If it is,
    // ArrayList will resize
    List<Byte> byteList = new ArrayList<Byte>(MAGIC_INITIAL_BYTE_ARRAY_SIZE);
    byte b;

    while ((b = in.readByte()) != terminalDelimiter) {
      byteList.add(b);
    }

    byteList.add(terminalDelimiter);

    return unpackComparable(byteListToArray(byteList), delimiter, terminalDelimiter);
  }

  public static byte[] packComparable(byte[]... arrays) {
    return packComparable(Arrays.asList(arrays));
  }

  public static byte[] packComparable(List<byte[]> arrays) {
    return packComparable(arrays, DEFAULT_DELIMITER, DEFAULT_TERMINAL_DELIMITER);
  }

  /**
   * note: the values delimiter and terminalDelimiter must not only NOT appear in the byte[], but be
   * less than any other value in the byte array. Defaults used in helper methods are 0 and 1. These
   * values in the packed byte[] to make it comparable as an unsigned byte []
   *
   * <p>A future extension would use duplication/padding (0 -> 00, 00 -> 000, etc) in order to
   * handle this, but it's not needed yet.
   *
   * <p>In practice, the byte values are for printable ascii chars and binary data may be base64
   * encoded as long as all values are byte values > 1
   *
   * @param arrays array of byte[] to pack
   * @param delimiter recommend 0
   * @param terminalDelimiter recommend 1
   * @return
   */
  public static byte[] packComparable(List<byte[]> arrays, byte delimiter, byte terminalDelimiter) {
    // item1,delim,item2,delim, ..., terminalDelmiter
    int packedSize = 0; // terminal delim length included below by overcount

    for (byte[] array : arrays) {
      packedSize += array.length + 1; // each item + delim
    }

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(packedSize);
    DataOutput output = new DataOutputStream(byteArrayOutputStream);

    try {
      boolean first = true;

      for (byte[] array : arrays) {
        if (!first) {
          output.write(delimiter);
        }

        output.write(array);
        first = false;
      }

      output.write(terminalDelimiter);

    } catch (IOException e) {
      throw new RuntimeException("no reason we should see this", e);
    }

    byte[] bytes = byteArrayOutputStream.toByteArray();

    return bytes;
  }

  public static List<byte[]> unpackComparable(byte[] packedArray) {
    return unpackComparable(packedArray, (byte) 1, (byte) 0);
  }

  public static List<byte[]> unpackComparable(
      byte[] packedArray, byte delimiter, byte terminalDelimiter) {
    List<byte[]> results = new ArrayList<byte[]>();
    List<Byte> currentToken = new ArrayList<Byte>(MAGIC_INITIAL_BYTE_ARRAY_SIZE); // very magic

    for (int i = 0; i < packedArray.length; i++) {
      if (packedArray[i] == terminalDelimiter) {
        results.add(byteListToArray(currentToken));
        // end of entire byte array
        break;
      } else if (packedArray[i] == delimiter) {
        // end of an element, store and move to next
        results.add(byteListToArray(currentToken));
        currentToken = new ArrayList<Byte>(MAGIC_INITIAL_BYTE_ARRAY_SIZE);
      } else {
        // put byte into current array
        currentToken.add(packedArray[i]);
      }
    }

    return results;
  }

  /**
   * packs a list of byte[] into a single array in the format
   *
   * <p>{@literal <numItems><len1,len2,...len_n><item1,item2,...item_n> }
   *
   * @param arrayList
   * @return packed byte array
   */
  public static byte[] pack(List<byte[]> arrays) {
    // numItems(short) + len1(int) + len2 + ...
    int packedSize = 2 + 4 * arrays.size();

    for (byte[] array : arrays) {
      packedSize += array.length;
    }
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(packedSize);
    DataOutput output = new DataOutputStream(byteArrayOutputStream);

    try {
      output.writeShort(arrays.size());

      for (byte[] array : arrays) {
        output.writeInt(array.length);
      }

      for (byte[] array : arrays) {
        output.write(array);
      }

    } catch (IOException e) {
      throw new RuntimeException("no reason we should see this", e);
    }

    byte[] bytes = byteArrayOutputStream.toByteArray();

    return bytes;
  }

  /**
   * unpack an array packed by pack()
   *
   * @param packedArray
   * @return
   * @see #pack(byte[]...)
   */
  public static byte[][] unpack(byte[] packedArray) {
    try {
      DataInput input = new DataInputStream(new ByteArrayInputStream(packedArray));

      short numItems = input.readShort();

      assert numItems >= 0;

      int[] lens = new int[numItems];
      byte[][] arrays = new byte[numItems][];

      for (int i = 0; i < numItems; i++) {
        lens[i] = input.readInt();
      }

      for (int i = 0; i < numItems; i++) {
        arrays[i] = new byte[lens[i]];

        input.readFully(arrays[i]);
      }

      return arrays;
    } catch (IOException e) {
      throw new RuntimeException("shouldn't see this either", e);
    }
  }

  /**
   * get an element from a packed array
   *
   * @param packedArray
   * @param pos
   * @return
   * @see #pack(byte[]...)
   */
  public static byte[] getElement(byte[] packedArray, int pos) {
    short numItems = (short) ((packedArray[0] << 8) | packedArray[1]);

    if (pos > numItems - 1) {
      throw new IllegalArgumentException(
          String.format("index %d is greater than max %d", pos, numItems - 1));
    }

    int i = 0;
    // numItems(short) + itemLen1 + itemLen2 + ...
    int dataPtr = 2 + numItems * 4;
    int dataLen = 0;
    while (i < numItems) {
      int j = 2 + (i * 4);
      int len = byteToInt(packedArray, j);

      assert len >= 0;

      if (i < pos) {
        dataPtr += len;
      } else {
        dataLen = len;
        break;
      }

      i++;
    }

    return Arrays.copyOfRange(packedArray, dataPtr, dataPtr + dataLen);
  }

  /**
   * not used, but alternative method to convert byte[4] -> int
   *
   * @param bytes
   * @param i
   * @return
   */
  public static int byteToIntAlt(byte[] bytes, int i) {
    DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes, i, 4));
    int value;

    try {
      value = stream.readInt();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return value;
  }

  public static byte[] byteListToArray(List<Byte> byteList) {
    byte[] result = new byte[byteList.size()];

    int i = 0;
    for (Byte b : byteList) {
      result[i++] = b;
    }

    return result;
  }

  /**
   * copied from java's Bits.getInt() method which is what's used underneath the
   * DataOutput.writeInt() above
   *
   * @param bytes array containing a 4-byte integer to convert
   * @param offset offset where 4-byte integer starts
   * @return
   */
  public static int byteToInt(byte[] bytes, int offset) {
    return ((bytes[offset + 3] & 0xFF))
        + ((bytes[offset + 2] & 0xFF) << 8)
        + ((bytes[offset + 1] & 0xFF) << 16)
        + ((bytes[offset]) << 24);
  }
}
