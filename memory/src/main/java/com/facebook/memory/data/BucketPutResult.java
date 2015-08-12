package com.facebook.memory.data;

import com.google.common.base.Preconditions;

import com.facebook.memory.data.structures.AnnotatableMemoryAddress;
import com.facebook.memory.data.structures.AnnotatedOffHeapValue;

/**
 * contains the result of a put(key,value) to a bucket. This will be either a new entry that can have an annotation
 * stored, or an existing entry (with the old value) that contains the annotation address
 */
public class BucketPutResult {
  private final AnnotatableMemoryAddress newEntry;
  private final AnnotatedOffHeapValue existingEntry;

  private BucketPutResult(AnnotatableMemoryAddress newEntry, AnnotatedOffHeapValue existingEntry ) {
    Preconditions.checkArgument(newEntry == null || existingEntry == null, "must be either new or existing");
    this.newEntry = newEntry;
    this.existingEntry = existingEntry;
  }

  public static BucketPutResult createExistingEntry(AnnotatedOffHeapValue existingEntry) {
    return new BucketPutResult(null, existingEntry);
  }

  public static BucketPutResult createNewEntry(AnnotatableMemoryAddress newEntry) {
    return new BucketPutResult(newEntry, null);
  }

  public AnnotatableMemoryAddress getNewEntry() {
    return newEntry;
  }

  public AnnotatedOffHeapValue getExistingEntry() {
    return existingEntry;
  }

  public boolean isExisting() {
    return existingEntry != null;
  }
}
