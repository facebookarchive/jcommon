package com.facebook.memory.data.types.definitions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Struct {
  private final Class<?> thisClass;
  private final Struct parent;
  private final AtomicInteger offset;
  private final Deque<Field> fields = new ArrayDeque<>();
  private final Supplier<List<Field>> mergedFieldListSupplier;

  private volatile boolean isTerminated = false;

  public Struct(Class<?> thisClass, Struct parent) {
    this.thisClass = thisClass;

    int initialOffset = parent == null ? 0 : parent.getOffset();

    offset = new AtomicInteger(initialOffset);
    this.parent = parent;
    mergedFieldListSupplier = Suppliers.memoize(this::internalGetMergedFieldsList);
  }

  public int getOffset() {
    return offset.get();
  }

  public void terminate() {
    isTerminated = true;
  }

  public void updateStruct(FieldType fieldType) {
    // ignore requests to measure the struct
    if (fieldType != FieldType.MEASURE) {
      fields.add(new Field(fieldType, offset.getAndAdd(fieldType.getSize())));
    }
  }

  public boolean isTerminated() {
    return isTerminated;
  }

  public Class<?> getThisClass() {
    return thisClass;
  }

  public Struct getParent() {
    return parent;
  }

  public List<Field> getMergedFieldList() {
    return mergedFieldListSupplier.get();
  }

  /**
   * this pushes fields from struct to struct.parent ... in reverse order, then reverses that
   * this = [a,b,c]
   * parent = [d,e]
   *
   * returns [e,d,c,b,a]
   * @return
   */
  private List<Field> internalGetMergedFieldsList() {
    Struct node = parent;
    Deque<Field> mergedFieldList = new ArrayDeque<>();

    fields.forEach(mergedFieldList::push);

    while (node != null) {
      node.fields.descendingIterator().forEachRemaining(mergedFieldList::push);
      node = node.getParent();
    }

    return mergedFieldList.stream().collect(Collectors.toList());
  }

  public static class Field {
    private final FieldType fieldType;
    private final int offset;

    public Field(FieldType fieldType, int offset) {
      this.fieldType = fieldType;
      this.offset = offset;
    }

    public FieldType getFieldType() {
      return fieldType;
    }

    public int getOffset() {
      return offset;
    }
  }
}
