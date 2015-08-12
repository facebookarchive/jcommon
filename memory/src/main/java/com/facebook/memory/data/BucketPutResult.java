package com.facebook.memory.data;

import com.google.common.base.Preconditions;

import com.facebook.memory.data.structures.AnnotatableMemoryAddress;
import com.facebook.memory.data.structures.AnnotatedOffHeapValue;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;

public class BucketPutResult {
  private final AnnotatableMemoryAddress newEntry;
  private final AnnotatedOffHeapValue existingEntry;

  private BucketPutResult(AnnotatableMemoryAddress newEntry, AnnotatedOffHeapValue existingEntry ) {
    Preconditions.checkArgument(newEntry == null || existingEntry == null, "must be either new or existing");
    this.newEntry = newEntry;
    this.existingEntry = existingEntry;
  }

  public static
  <V extends SizedOffHeapStructure> BucketPutResult createExistingEntry(AnnotatedOffHeapValue existingEntry) {
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
