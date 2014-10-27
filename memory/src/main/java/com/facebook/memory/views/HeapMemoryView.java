package com.facebook.memory.views;

import com.google.common.base.Preconditions;

import com.facebook.memory.MemoryConstants;

public class HeapMemoryView implements MemoryView {
  private static final long MASK = 0xFF;

  private final byte[] bytes;
  private int current;

  public HeapMemoryView(byte[] bytes, int current) {
    this.bytes = bytes;
    this.current = current;
  }

  public static Factory factory() {
    return new Factory();
  }

  @Override
  public boolean hasNextByte() {
    return current < bytes.length;
  }

  @Override
  public byte nextByte() {
    Preconditions.checkState(current < bytes.length);

    byte read = bytes[current];

    current++;

    return read;
  }

  @Override
  public short nextShort() {
    short read = (short) (bytes[current] << Byte.SIZE & bytes[current + 1]);

    current += Short.BYTES;

    return read;
  }

  @Override
  public int nextInt() {
    int read = bytes[current] << Byte.SIZE * 3 &
      bytes[current + 1] << Byte.SIZE * 2 &
      bytes[current + 2] << Byte.SIZE &
      bytes[current + 3];

    current += Integer.BYTES;

    return read;

  }

  @Override
  public long nextLong() {
    long read = getLong(current);

    current += Long.BYTES;

    return read;
  }

  @Override
  public long nextPointer() {
    return nextLong();
  }

  @Override
  public byte nextByte(byte b) {
    Preconditions.checkState(current < bytes.length);

    byte read = bytes[current];

    bytes[current] = b;
    current++;

    return read;
  }

  @Override
  public short nextShort(short s) {
    short read = getShort(current);

    putShort(current, s);
    current += Short.BYTES;

    return read;
  }

  @Override
  public int nextInt(int i) {
    int read = getInt(current);

    putInt(current, i);
    current += Integer.BYTES;

    return read;
  }

  @Override
  public long nextLong(long l) {
    long read = getLong(current);

    putLong(current, l);
    current += Long.BYTES;

    return read;
  }

  @Override
  public long nextPointer(long p) {
    return nextLong(p);
  }

  @Override
  public byte getByte(int byteOffset) {
    Preconditions.checkState(byteOffset < bytes.length);

    return bytes[byteOffset];
  }

  @Override
  public short getShort(int byteOffset) {
    Preconditions.checkState(byteOffset < bytes.length);

    short read = (short) (((bytes[byteOffset] & 0xFF) << (Byte.SIZE)) |
                            (bytes[byteOffset + 1] & 0xFF));

    return read;
  }

  @Override
  public int getInt(int byteOffset) {
    Preconditions.checkState(byteOffset < bytes.length);

    int read = ((bytes[byteOffset] & 0xFF) << (3 * Byte.SIZE)) |
      ((bytes[byteOffset + 2] & 0xFF) << (2 * Byte.SIZE)) |
      ((bytes[byteOffset + 3] & 0xFF) << (Byte.SIZE)) |
      (bytes[byteOffset + 4] & 0xFF);

    return read;
  }

  @Override
  public long getLong(int byteOffset) {
    long read = ((bytes[byteOffset] & 0xFFL) << (7 * Byte.SIZE)) |
      ((bytes[byteOffset + 1] & 0xFFL) << (6 * Byte.SIZE)) |
      ((bytes[byteOffset + 2] & 0xFFL) << (5 * Byte.SIZE)) |
      ((bytes[byteOffset + 3] & 0xFFL) << (4 * Byte.SIZE)) |
      ((bytes[byteOffset + 4] & 0xFFL) << (3 * Byte.SIZE)) |
      ((bytes[byteOffset + 5] & 0xFFL) << (2 * Byte.SIZE)) |
      ((bytes[byteOffset + 6] & 0xFFL) << (Byte.SIZE)) |
      (bytes[byteOffset + 7] & 0xFFL);

    return read;
  }

  @Override
  public long getPointer(int byteOffset) {
    return getLong(byteOffset);
  }

  @Override
  public void putByte(int byteOffset, byte b) {
    Preconditions.checkState(byteOffset < bytes.length);

    bytes[byteOffset] = b;
  }

  @Override
  public void putShort(int byteOffset, short s) {
    putBytes(byteOffset, s, Short.BYTES);
  }

  @Override
  public void putInt(int byteOffset, int i) {
    putBytes(byteOffset, i, Integer.BYTES);
  }

  @Override
  public void putLong(int byteOffset, long l) {
    putBytes(byteOffset, l, Long.BYTES);
  }

  private void putBytes(int byteOffset, long param, int numBytes) {
    for (int i = 0; i < numBytes; i++) {
      int value = (int) (param & MASK);
      int index = byteOffset + (numBytes - i - 1);

      bytes[index] = (byte) value;
      param >>= Byte.SIZE;
    }
  }

  @Override
  public void putPointer(int byteOffset, long p) {
    putLong(byteOffset, p);
  }

  @Override
  public long getAddress() {
    return MemoryConstants.NO_ADDRESS;
  }

  @Override
  public long getSize() {
    return bytes.length;
  }

  @Override
  public long getCurrent() {
    return current;
  }

  @Override
  public long getMaxSize() {
    return bytes.length;
  }

  public static class Factory {
    private Factory() {
    }

    public MemoryView create(int size) {
      return new HeapMemoryView(new byte[size], 0);
    }

    public MemoryView create(byte[] bytes) {
      return new HeapMemoryView(bytes, 0);
    }

    public MemoryView create(HeapMemoryView other) {
      return new HeapMemoryView(other.bytes, 0);
    }
  }

}
