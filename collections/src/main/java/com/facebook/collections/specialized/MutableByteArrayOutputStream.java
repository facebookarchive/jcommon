package com.facebook.collections.specialized;

import com.facebook.collections.ByteArray;

import java.io.ByteArrayOutputStream;

/**
 * allows direct access to the underlying buffer without a copy
 * also has a higher default size
 */
public class MutableByteArrayOutputStream extends ByteArrayOutputStream {
  
  public MutableByteArrayOutputStream() {
    // use a higher default size
    this(1024);
  }

  public MutableByteArrayOutputStream(int size) {
    super(size);
  }
  
  public ByteArray getBytes() {
    return ByteArray.wrap(buf, 0, size());
  }
}
