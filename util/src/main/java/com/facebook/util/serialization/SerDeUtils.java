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
