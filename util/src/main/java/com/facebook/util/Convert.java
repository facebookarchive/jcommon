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
package com.facebook.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Various helper functions */
public class Convert {
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  public static ByteBuffer toByteBuffer(String str) {
    if (str == null) {
      return null;
    }

    // Use UTF8 to keep it consistent with Convert.toString()
    return UTF8.encode(str);
  }

  // just to make it consistent with the other conversion functions
  public static ByteBuffer toByteBuffer(byte[] bytes) {
    return ByteBuffer.wrap(bytes);
  }

  public static String toString(ByteBuffer bb) {
    // Don't use Bytes.toString() here since there can be an extra array copy
    // to convert ByteBuffer to Bytes (see Convert.toBytes())

    if (bb == null) {
      return null;
    }

    return UTF8.decode(bb).toString();
  }

  /**
   * Get byte array from ByteBuffer. This function returns a byte array reference that has exactly
   * the same valid range as the ByteBuffer. Note that you should not write to the resulting byte
   * array directly. If you want a writable copy, please use
   * org.apache.hadoop.hbase.util.Bytes.toBytes(ByteBuffer).
   *
   * @param bb the byte buffer
   * @return a reference to a byte array that contains the same content as the given ByteBuffer
   */
  public static byte[] toBytes(ByteBuffer bb) {
    // we cannot call array() on read only or direct ByteBuffers
    if (bb.isReadOnly() || bb.isDirect()) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream(bb.limit());
        Channels.newChannel(out).write(bb);
        return out.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e); // memory error
      }
    } else if (bb.array().length == bb.limit()) {
      return bb.array();
    } else {
      return Arrays.copyOfRange(bb.array(), bb.arrayOffset(), bb.arrayOffset() + bb.limit());
    }
  }
}
