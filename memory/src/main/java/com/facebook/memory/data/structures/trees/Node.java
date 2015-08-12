package com.facebook.memory.data.structures.trees;

import sun.misc.Unsafe;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

class Node implements Comparable<Node> {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private final long address;

  private Node(long address) {
    this.address = address;
  }

  public static Node create(long address, long dataAddress, int dataSize) {
    Node node = new Node(address);

    node.setDataAddress(dataAddress);
    node.setDataSize(dataSize);
    node.setLeftPtr(MemoryConstants.NO_ADDRESS);
    node.setRightPtr(MemoryConstants.NO_ADDRESS);

    return node;
  }

  public static int size() {
    return 3 * UNSAFE.addressSize() + Integer.BYTES;
  }

  public Span getSpan() {
    return Span.from(address);
  }

  public long getDataAddress() {
    return UNSAFE.getAddress(address);
  }

  public int getDataSize() {
    return UNSAFE.getInt(address + UNSAFE.addressSize());
  }

  public long getLeftPtr() {
    return UNSAFE.getAddress(address + UNSAFE.addressSize() + Integer.BYTES);
  }

  public long getRightPtr() {
    return UNSAFE.getAddress(address + 2 * UNSAFE.addressSize() + Integer.BYTES);
  }

  public Node getLeftNode() {
    return new Node(getLeftPtr());
  }

  public Node getRightNode() {
    return new Node(getRightPtr());
  }

  public long getAddress() {
    return address;
  }

  public void setDataAddress(long dataAddress) {
    UNSAFE.putAddress(address, dataAddress);
  }

  public void setDataSize(int dataSize) {
    UNSAFE.putInt(address + UNSAFE.addressSize(), dataSize);
  }

  public void setLeftPtr(long leftPtr) {
    UNSAFE.putAddress(address + UNSAFE.addressSize() + Integer.BYTES, leftPtr);
  }

  public void setRightPtr(long rightPtr) {
    UNSAFE.putAddress(address + 2 * UNSAFE.addressSize() + Integer.BYTES, rightPtr);
  }

  @Override
  public int compareTo(Node o) {
    if (this == o) {
      return 0;
    }

    if (o == null) {
      return 1;
    }

    return Long.signum(getDataAddress() - o.getDataAddress());
  }
}
