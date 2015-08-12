package com.facebook.memory.data.structures;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

/**
 * stores an annotation address along with an offheap value. The annotation address is used to link some offheap data
 * with some other bookkeeping inforamtion (eg cache policy entries)
 */
public class AnnotatedOffHeapValue implements Annotated {
  private final SizedOffHeapStructure value;
  private final long annotationAddress;

  public AnnotatedOffHeapValue(SizedOffHeapStructure value, long annotationAddress) {
    this.value = value;
    this.annotationAddress = annotationAddress;
  }

  @Override
  public long getAnnotationAddress() {
    return annotationAddress;
  }

  public SizedOffHeapStructure getValue() {
    return value;
  }
}
