package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

/**
 * for a LinkedListBucketNode, this class will return the value portion
 */
public class LinkedListValueWrapper implements SizedOffHeapWrapper {
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;

  public LinkedListValueWrapper(
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
  }

  @Override
  public SizedOffHeapStructure wrap(long address) {
    return LinkedListBucketNode.wrap(address, keyWrapper, valueWrapper).getValue();
  }
}
