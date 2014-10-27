package com.facebook.memory.views;

import java.util.List;

// TODO: creates a single view over a set of MemoryViews
public class CompositeMemoryView implements MemoryView {
  private final List<MemoryView> memoryViewList;

  public CompositeMemoryView(List<MemoryView> memoryViewList) {
    this.memoryViewList = memoryViewList;
  }

  @Override
  public boolean hasNextByte() {
    return false;
  }

  @Override
  public byte nextByte() {
    return 0;
  }

  @Override
  public short nextShort() {
    return 0;
  }

  @Override
  public int nextInt() {
    return 0;
  }

  @Override
  public long nextLong() {
    return 0;
  }

  @Override
  public long nextPointer() {
    return 0;
  }

  @Override
  public byte nextByte(byte b) {
    return 0;
  }

  @Override
  public short nextShort(short s) {
    return 0;
  }

  @Override
  public int nextInt(int i) {
    return 0;
  }

  @Override
  public long nextLong(long l) {
    return 0;
  }

  @Override
  public long nextPointer(long p) {
    return 0;
  }

  @Override
  public byte getByte(int byteOffset) {
    return 0;
  }

  @Override
  public short getShort(int byteOffset) {
    return 0;
  }

  @Override
  public int getInt(int byteOffset) {
    return 0;
  }

  @Override
  public long getLong(int byteOffset) {
    return 0;
  }

  @Override
  public long getPointer(int byteOffset) {
    return 0;
  }

  @Override
  public void putByte(int byteOffset, byte b) {

  }

  @Override
  public void putShort(int byteOffset, short s) {

  }

  @Override
  public void putInt(int byteOffset, int i) {

  }

  @Override
  public void putLong(int byteOffset, long l) {

  }

  @Override
  public void putPointer(int byteOffset, long p) {

  }

  @Override
  public long getAddress() {
    return 0;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public long getCurrent() {
    return 0;
  }

  @Override
  public long getMaxSize() {
    return 0;
  }
}
