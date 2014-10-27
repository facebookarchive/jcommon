package com.facebook.memory.views;

import com.google.common.base.Preconditions;
import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public abstract class AbstractMemoryView<T extends Comparable<T>> implements MemoryView {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long address;

  protected AbstractMemoryView(long address) {
    this.address = address;
  }

  protected abstract T current();

  protected abstract long currentAsLong();

  protected abstract T size();

  protected abstract long sizeAsLong();

  protected abstract long maxSize();

  protected abstract long adjustedAddress(T value);

  protected abstract T incrementCurrent(int value);

  @Override
  public boolean hasNextByte() {
    return current().compareTo(size()) < 0;
  }

  @Override
  public byte nextByte() {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    byte read = UNSAFE.getByte(adjustedAddress());

    incrementCurrent(Byte.BYTES);

    return read;
  }

  @Override
  public short nextShort() {
    Preconditions.checkState(current().compareTo(size()) < 0 );

    short read = UNSAFE.getShort(adjustedAddress());

    incrementCurrent(Short.BYTES);

    return read;
  }

  @Override
  public int nextInt() {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    int read = UNSAFE.getInt(adjustedAddress());

    incrementCurrent(Integer.BYTES);

    return read;
  }

  @Override
  public long nextLong() {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    long read = UNSAFE.getLong(adjustedAddress());

    incrementCurrent(Long.BYTES);

    return read;

  }

  @Override
  public long nextPointer() {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    long read = UNSAFE.getAddress(adjustedAddress());

    incrementCurrent(UNSAFE.addressSize());

    return read;

  }

  @Override
  public byte nextByte(byte b) {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    byte read = UNSAFE.getByte(adjustedAddress());

    UNSAFE.putByte(adjustedAddress(), b);
    incrementCurrent(Byte.BYTES);

    return read;
  }

  @Override
  public short nextShort(short s) {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    short read = UNSAFE.getShort(adjustedAddress());

    UNSAFE.putShort(adjustedAddress(), s);
    incrementCurrent(Short.BYTES);

    return read;

  }

  @Override
  public int nextInt(int i) {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    int read = UNSAFE.getInt(adjustedAddress());

    UNSAFE.putInt(adjustedAddress(), i);
    incrementCurrent(Integer.BYTES);

    return read;
  }

  @Override
  public long nextLong(long l) {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    long read = UNSAFE.getLong(adjustedAddress());

    UNSAFE.putLong(adjustedAddress(), l);
    incrementCurrent(Long.BYTES);


    return read;
  }

  @Override
  public long nextPointer(long p) {
    Preconditions.checkState(current().compareTo(size()) < 0 );
    long read = UNSAFE.getAddress(adjustedAddress());

    UNSAFE.putAddress(adjustedAddress(), p);
    incrementCurrent(UNSAFE.addressSize());

    return read;
  }


  @Override
  public byte getByte(int byteOffset) {
    byte b = UNSAFE.getByte(address + byteOffset);

    return b;
  }

  @Override
  public short getShort(int byteOffset) {
    short s = UNSAFE.getShort(address + byteOffset);

    return s;
  }

  @Override
  public int getInt(int byteOffset) {
    int i = UNSAFE.getInt(address + byteOffset);

    return i;
  }

  @Override
  public long getLong(int byteOffset) {
    long l = UNSAFE.getLong(address + byteOffset);

    return l;
  }

  @Override
  public long getPointer(int byteOffset) {
    long l = UNSAFE.getAddress(address + byteOffset);

    return l;
  }

  @Override
  public void putByte(int byteOffset, byte b) {
    UNSAFE.putByte(address + byteOffset, b);
  }

  @Override
  public void putShort(int byteOffset, short s) {
    UNSAFE.putShort(address + byteOffset, s);
  }

  @Override
  public void putInt(int byteOffset, int i) {
    Preconditions.checkArgument(currentAsLong() + byteOffset < sizeAsLong());

    UNSAFE.putInt(address + byteOffset, i);
  }

  @Override
  public void putLong(int byteOffset, long l) {
    UNSAFE.putLong(address + byteOffset, l);
  }

  @Override
  public void putPointer(int byteOffset, long p) {
    UNSAFE.putAddress(address + byteOffset, p);
  }

  @Override
  public long getAddress() {
    return address;
  }

  @Override
  public long getSize() {
    return sizeAsLong();
  }

  @Override
  public long getCurrent() {
    return currentAsLong();
  }

  @Override
  public long getMaxSize() {
    return maxSize();
  }

  private long adjustedAddress() {
    return adjustedAddress(current());
  }
}
