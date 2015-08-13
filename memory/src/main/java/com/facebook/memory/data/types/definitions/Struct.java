package com.facebook.memory.data.types.definitions;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Struct {
  private final Class<?> thisClass;
  private final Struct parent;
  private final Deque<Field> fields = new ArrayDeque<>();
  private final Map<Object, Field> fieldMap = new LinkedHashMap<>();
  private final AtomicInteger staticFieldsSize;


  public Struct(Class<?> thisClass, Struct parent) {
    this.thisClass = thisClass;
    this.parent = parent;

    if (parent != null) {
      fieldMap.putAll(parent.getFieldMap());
    }
    staticFieldsSize = new AtomicInteger(parent == null ? 0 : parent.getStaticFieldsSize());
  }

  protected Map<Object, Field> getFieldMap() {
    return Collections.unmodifiableMap(fieldMap);
  }

  public Class<?> getThisClass() {
    return thisClass;
  }

  public Struct getParent() {
    return parent;
  }

  public List<Field> getMergedFieldList() {
    Deque<Field> mergedFieldDeque = getMergedFieldDeque();
    List<Field> mergedFieldList = mergedFieldDeque.stream().collect(Collectors.toList());
    Collections.reverse(mergedFieldList);

    return mergedFieldList;
  }

  public Field getLastField() {
    Deque<Field> fieldDeque = getMergedFieldDeque();

    return fieldDeque.isEmpty() ? null : fieldDeque.getFirst();
  }

  public void updateStruct(Slot slot, FieldType fieldType, FieldOffsetMapper fieldOffsetMapper) {
    Field field = new Field(fieldType, fieldOffsetMapper);
    fields.add(field);
    staticFieldsSize.getAndAdd(fieldType.getStaticFieldsSize());
    fieldMap.put(slot, field);
  }

  public Field getField(Slot slot) {
    return fieldMap.get(slot);
  }

  public int getStaticFieldsSize() {
    return staticFieldsSize.get();
  }

  /**
   * returns fields in reverse order where if we declare (from parent down through children)
   *  Slot a;
   *  Slot b;
   *  ...
   *  Slot c;
   *
   *  declaration order is [a,b,c]
   *
   *  returns [c,b,a]
   *
   * @return
   */
  private Deque<Field> getMergedFieldDeque() {
    Struct node = parent;
    Deque<Field> mergedFieldDeque = new ArrayDeque<>();

    fields.descendingIterator().forEachRemaining(mergedFieldDeque::addLast);

    while (node != null) {
      node.fields.descendingIterator().forEachRemaining(mergedFieldDeque::addLast);
      node = node.getParent();
    }
    return mergedFieldDeque;
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
