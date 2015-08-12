package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.data.BucketPutResult;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
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
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;

  private volatile LinkedListBucketNode head;
  private volatile LinkedListBucketNode tail;
  private volatile int size = 0;

  private LinkedListBucket(
    Slab slab,
    MemoryView memoryView,
    LinkedListBucketNode head,
    LinkedListBucketNode tail,
    int size,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    this.slab = slab;
    this.head = head;
    this.tail = tail;
    this.memoryView = memoryView;
    this.size = size;
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
  }

  private LinkedListBucket(
    long address,
    Slab slab,
    MemoryViewFactory memoryViewFactory,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    this.slab = slab;
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
    memoryView = memoryViewFactory.wrap(address, SIZE_OF_OFF_HEAP_DATA);

    long headPointer = memoryView.getPointer(HEAD_ADDR_OFFSET);

    head = headPointer == MemoryConstants.NO_ADDRESS ? null : LinkedListBucketNode.wrap(
      headPointer,
      keyWrapper,
      valueWrapper
    );

    long tailPointer = memoryView.getPointer(TAIL_ADDR_OFFSET);

    tail = tailPointer == MemoryConstants.NO_ADDRESS ? null : LinkedListBucketNode.wrap(
      tailPointer,
      keyWrapper,
      valueWrapper
    );
    size = memoryView.getInt(SIZE_OFFSET);
  }

  public static LinkedListBucket wrap(
    long address,
    Slab slab,
    MemoryViewFactory memoryViewFactory,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    return new LinkedListBucket(address, slab, memoryViewFactory, keyWrapper, valueWrapper);
  }

  public static LinkedListBucket create(
    Slab slab,
    MemoryViewFactory memoryViewFactory,
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  )
    throws FailedAllocationException {
    long address = slab.allocate(SIZE_OF_OFF_HEAP_DATA);
    MemoryView memoryView = memoryViewFactory.wrap(address, SIZE_OF_OFF_HEAP_DATA);

    memoryView.putPointer(HEAD_ADDR_OFFSET, MemoryConstants.NO_ADDRESS);
    memoryView.putPointer(TAIL_ADDR_OFFSET, MemoryConstants.NO_ADDRESS);
    memoryView.putInt(SIZE_OFFSET, 0);

    return new LinkedListBucket(slab, memoryView, null, null, 0, keyWrapper, valueWrapper);
  }

  public long getAddress() {
    return memoryView.getAddress();
  }

  @Override
  public BucketPutResult put(SizedOffHeapStructure key, SizedOffHeapStructure value) throws FailedAllocationException {
    LinkedListBucketNode curr = head;

    for (int i = 0; i < size; i++) {
      if (curr.keyEquals(key)) {

        curr.replaceValue(value);

        return BucketPutResult.createExistingEntry(
          new AnnotatedOffHeapValue(
            curr.getValue(),
            curr.getAnnotationAddress()
          )
        );
      }

      if (curr.getAddress() != tail.getAddress()) {
        curr = curr.next();
      }
    }

    return BucketPutResult.createNewEntry(insertAtHead(key, value));
  }

  private AnnotatableMemoryAddress insertAtHead(SizedOffHeapStructure key, SizedOffHeapStructure value) throws FailedAllocationException {
    final LinkedListBucketNode curr = LinkedListBucketNode.create(slab, key, value, keyWrapper, valueWrapper);
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
  public BucketEntry get(SizedOffHeapStructure key) {
    LinkedListBucketNode prev = null;
    LinkedListBucketNode curr = head;

    for (int i = 0; i < size; i++) {
      if (curr.keyEquals(key)) {
        return new BucketEntryImpl(prev, curr, i == 0);
      }

      if (curr.getAddress() != tail.getAddress()) {
        prev = curr;
        curr = curr.next();
      }
    }

    return null;
  }

  @Override
  public boolean remove(SizedOffHeapStructure key) {
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
        removeEntry(curr, prev, false);
        return true;
      }
    }
    return false;
  }

  private void removeEntry(LinkedListBucketNode curr, LinkedListBucketNode prev, boolean isHead) {
    if (isHead) {
      LinkedListBucketNode tmp = size > 1 ? head.next() : null;
      slab.free(head.getAddress(), head.getSize());
      head = tmp;
      memoryView.putPointer(HEAD_ADDR_OFFSET, head == null ? 0 : head.getAddress());
      size--;
      memoryView.putInt(SIZE_OFFSET, size);
    } else {
      LinkedListBucketNode next = curr.getAddress() == tail.getAddress() ? null : curr.next();
      prev.setNext(next == null ? MemoryConstants.NO_ADDRESS : next.getAddress());
      size--;
      memoryView.putInt(SIZE_OFFSET, size);
      if (next == null) {
        tail = prev;
        memoryView.putPointer(TAIL_ADDR_OFFSET, tail.getAddress());
      }
      slab.free(curr.getAddress(), curr.getSize());
    }

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

  private class BucketEntryImpl implements BucketEntry {
    private final LinkedListBucketNode prev;
    private final LinkedListBucketNode curr;
    private final AnnotatedOffHeapValue value;
    private final boolean isHead;

    private BucketEntryImpl(LinkedListBucketNode prev, LinkedListBucketNode curr, boolean isHead) {
      this.prev = prev;
      this.curr = curr;
      this.isHead = isHead;
      value = new AnnotatedOffHeapValue(curr.getValue(), curr.getAnnotationAddress());
    }

    @Override
    public SizedOffHeapStructure getKey() {
      return curr.getKey();
    }

    @Override
    public AnnotatedOffHeapValue getAnnotatedValue() {
      return value;
    }

    @Override
    public void remove() {
      removeEntry(curr, prev, isHead);
    }

    @Override
    public long getAddress() {
      return curr.getAddress();
    }
  }
}
