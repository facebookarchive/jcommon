package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

/**
 * for a LinkedListBucketNode, this class will return the key portion
 */
public class LinkedListKeyWrapper implements SizedOffHeapWrapper {
  private final SizedOffHeapWrapper keyWrapper;
  private final SizedOffHeapWrapper valueWrapper;

  public LinkedListKeyWrapper(
    SizedOffHeapWrapper keyWrapper,
    SizedOffHeapWrapper valueWrapper
  ) {
    this.keyWrapper = keyWrapper;
    this.valueWrapper = valueWrapper;
  }

  @Override
  public SizedOffHeapStructure wrap(long address) {
    return LinkedListBucketNode.wrap(address, keyWrapper, valueWrapper).getKey();
  }
}
