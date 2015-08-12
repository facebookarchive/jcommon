package com.facebook.memory.data.types.definitions;

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

  public Struct(Class<?> thisClass, Struct parent) {
    this.thisClass = thisClass;

    int initialOffset = parent == null ? 0 : parent.getOffset();

    offset = new AtomicInteger(initialOffset);
    this.parent = parent;
  }

  public int getOffset() {
    return offset.get();
  }

  public void updateStruct(FieldType fieldType, FieldOffsetMapper fieldOffsetMapper) {
    fields.add(new Field(fieldType, fieldOffsetMapper));
  }

  public Class<?> getThisClass() {
    return thisClass;
  }

  public Struct getParent() {
    return parent;
  }

  public List<Field> getMergedFieldList() {
    Deque<Field> mergedFieldList = getMergedFieldDeque();

    return mergedFieldList.stream().collect(Collectors.toList());
  }

  public Field getLastField() {
    Deque<Field> fieldDeque = getMergedFieldDeque();

    return fieldDeque.isEmpty() ? null : fieldDeque.getFirst();
  }

  private Deque<Field> getMergedFieldDeque() {
    Struct node = parent;
    Deque<Field> mergedFieldList = new ArrayDeque<>();

    fields.forEach(mergedFieldList::push);

    while (node != null) {
      node.fields.descendingIterator().forEachRemaining(mergedFieldList::push);
      node = node.getParent();
    }
    return mergedFieldList;
  }

  public static class Field {
    private final FieldType fieldType;
    private final FieldOffsetMapper fieldOffsetMapper;

    public Field(FieldType fieldType, FieldOffsetMapper fieldOffsetMapper) {
      this.fieldType = fieldType;
      this.fieldOffsetMapper = fieldOffsetMapper;
    }

    public FieldType getFieldType() {
      return fieldType;
    }

    public FieldOffsetMapper getFieldOffsetMapper() {
      return fieldOffsetMapper;
    }

    public int getOffset(long address) {
      return fieldOffsetMapper.getFieldStartOffset(address);
    }
  }
}
