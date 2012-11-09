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
package com.facebook.util.serialization;


import com.facebook.util.Convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public class SerDeUtils {
  public static <T> byte[] serializeToBytes(T value, Serializer<T> serializer)
    throws SerDeException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    
    serializer.serialize(value, new DataOutputStream(out));
    
    return out.toByteArray();
  }

  public static <T> ByteBuffer serializeToByteBuffer(
    T value, Serializer<T> serializer
  ) throws SerDeException {
    return ByteBuffer.wrap(serializeToBytes(value, serializer));
  }

  public static <T> T deserializeFromBytes(
    byte[] bytes, Deserializer<T> deserializer
  ) throws SerDeException {
    
    if (bytes == null) {
      return null;
    }
    
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);

    return deserializer.deserialize(new DataInputStream(in));
  }

  public static <T> T deserializeFromByteBuffer(
    ByteBuffer buffer, Deserializer<T> deserializer
  ) throws SerDeException {
    
    if (buffer == null) {
      return null;
    }
    
    ByteArrayInputStream in = new ByteArrayInputStream(Convert.toBytes(buffer));

    return deserializer.deserialize(new DataInputStream(in));
  }
}
