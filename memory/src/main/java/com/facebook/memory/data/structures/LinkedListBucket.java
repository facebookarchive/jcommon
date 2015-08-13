package com.facebook.memory.data.structures;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewFactory;

public class LinkedListBucket implements Bucket {
  private static final int SIZE_OF_OFF_HEAP_DATA = MemoryConstants.ADDRESS_SIZE * 2 + Integer.BYTES;
  private static final int HEAD_ADDR_OFFSET = 0;
  private static final int TAIL_ADDR_OFFSET = HEAD_ADDR_OFFSET + MemoryConstants.ADDRESS_SIZE;
  private static final int SIZE_OFFSET = TAIL_ADDR_OFFSET + MemoryConstants.ADDRESS_SIZE;

  private final Slab slab;
  private final MemoryView memoryView;

  private volatile LinkedListBucketNode head;
  private volatile LinkedListBucketNode tail;
  private volatile int size = 0;

  private LinkedListBucket(
    Slab slab,
    MemoryView memoryView,
    LinkedListBucketNode head,
    LinkedListBucketNode tail,
    int size
  ) {
    this.slab = slab;
    this.head = head;
    this.tail = tail;
    this.memoryView = memoryView;
    this.size = size;
  }

  private LinkedListBucket(long address, Slab slab, MemoryViewFactory memoryViewFactory) {
    this.slab = slab;
    memoryView = memoryViewFactory.wrap(address, SIZE_OF_OFF_HEAP_DATA);

    long headPointer = memoryView.getPointer(HEAD_ADDR_OFFSET);

    head = headPointer == MemoryConstants.NO_ADDRESS ? null : LinkedListBucketNode.wrap(headPointer);

    long tailPointer = memoryView.getPointer(TAIL_ADDR_OFFSET);

    tail = tailPointer == MemoryConstants.NO_ADDRESS ? null : LinkedListBucketNode.wrap(tailPointer);
    size = memoryView.getInt(SIZE_OFFSET);
  }

  public static LinkedListBucket wrap(long address, Slab slab, MemoryViewFactory memoryViewFactory) {
    return new LinkedListBucket(address, slab, memoryViewFactory);
  }

  public static LinkedListBucket create(Slab slab, MemoryViewFactory memoryViewFactory)
    throws FailedAllocationException {
    long address = slab.allocate(SIZE_OF_OFF_HEAP_DATA);
    MemoryView memoryView = memoryViewFactory.wrap(address, SIZE_OF_OFF_HEAP_DATA);

    memoryView.putPointer(HEAD_ADDR_OFFSET, MemoryConstants.NO_ADDRESS);
    memoryView.putPointer(TAIL_ADDR_OFFSET, MemoryConstants.NO_ADDRESS);
    memoryView.putInt(SIZE_OFFSET, 0);

    return new LinkedListBucket(slab, memoryView, null, null, 0);
  }

  public long getAddress() {
    return memoryView.getAddress();
  }

  @Override
  public AnnotatableMemoryAddress put(ByteArray key, ByteArray value) throws FailedAllocationException {
    final LinkedListBucketNode curr = LinkedListBucketNode.create(key, value, slab);
    if (head == null) {
      head = tail = curr;
      memoryView.putPointer(HEAD_ADDR_OFFSET, head.getAddress());
    } else {
      LinkedListBucketNode tmp = tail;
      tail = curr;
      tmp.setNext(tail.getAddress());
    }
    size++;
    memoryView.putPointer(TAIL_ADDR_OFFSET, tail.getAddress());
    memoryView.putInt(SIZE_OFFSET, size);
    return new AnnotatableMemoryAddress(curr.getAddress()) {
      @Override
      public void storeAnnotationAddress(OffHeap offHeap) {
        curr.storeAnnotationAddress(offHeap);
      }
    };
  }

  @Override
  public AnnotatedByteArray get(ByteArray key) {
    LinkedListBucketNode curr = head;
    for (int i = 0; i < size; i++) {
      if (curr.keyEquals(key)) {
        return new AnnotatedByteArray(curr.getAddress(), curr.getAnnotationAddress(), curr.getValue());
      }

      if (curr.getAddress() != tail.getAddress()) {
        curr = curr.next();
      }
    }

    return null;
  }

  @Override
  public boolean remove(ByteArray key) {
    if (head.keyEquals(key)) {
      LinkedListBucketNode tmp = size > 1 ? head.next() : null;
      slab.free(head.getAddress(), head.getSize());
      head = tmp;
      memoryView.putPointer(HEAD_ADDR_OFFSET, head == null ? 0 : head.getAddress());
      size--;
      memoryView.putInt(SIZE_OFFSET, size);
      return true;
    }

    if (size == 1) {
      return false;
    }

    LinkedListBucketNode curr = head;
    for (int i = 1; i < size; i++) {
      LinkedListBucketNode prev = curr;
      curr = curr.next();
      if (curr.keyEquals(key)) {
        LinkedListBucketNode next = curr.getAddress() == tail.getAddress() ? null : curr.next();
        slab.free(curr.getAddress(), curr.getSize());
        prev.setNext(next == null ? MemoryConstants.NO_ADDRESS : next.getAddress());
        size--;
        memoryView.putInt(SIZE_OFFSET, size);
        if (next == null) {
          tail = prev;
          memoryView.putPointer(TAIL_ADDR_OFFSET, tail.getAddress());
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public int hashCode() {
    return (int) memoryView.getAddress() % Integer.MAX_VALUE;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null &&
      obj instanceof LinkedListBucket &&
      ((LinkedListBucket) obj).getAddress() == memoryView.getAddress();
  }
}
