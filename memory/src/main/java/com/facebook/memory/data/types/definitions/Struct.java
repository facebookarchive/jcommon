package com.facebook.memory.data.types.definitions;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * only one thread at a time will access during class initialization (since jvm initializes static fields in order)
 * and we create at most one of these for a given class being initialized
 */
@NotThreadSafe
public class Struct {
  private final Class<?> thisClass;
  private final Struct parent;
  private final Deque<SlotInfo> slotInfos = new ArrayDeque<>();
  private final Map<Object, SlotInfo> slotInfoMap = new LinkedHashMap<>();
  private final AtomicInteger staticSlotsSize;


  public Struct(Class<?> thisClass, Struct parent) {
    this.thisClass = thisClass;
    this.parent = parent;

    if (parent != null) {
      slotInfoMap.putAll(parent.getSlotInfoMap());
    }
    staticSlotsSize = new AtomicInteger(parent == null ? 0 : parent.getStaticSlotsSize());
  }

  protected Map<Object, SlotInfo> getSlotInfoMap() {
    return Collections.unmodifiableMap(slotInfoMap);
  }

  public Class<?> getThisClass() {
    return thisClass;
  }

  public Struct getParent() {
    return parent;
  }

  public List<SlotInfo> getMergedSlotInfoList() {
    Deque<SlotInfo> mergedSlotInfoDeque = getMergedSlotInfoDequeue();
    List<SlotInfo> mergedSlotInfoList = mergedSlotInfoDeque.stream().collect(Collectors.toList());
    Collections.reverse(mergedSlotInfoList);

    return mergedSlotInfoList;
  }

  public SlotInfo getLastSlotInfo() {
    Deque<SlotInfo> slotInfoDeque = getMergedSlotInfoDequeue();

    return slotInfoDeque.isEmpty() ? null : slotInfoDeque.getFirst();
  }

  public void updateStruct(Slot slot, SlotType slotType, SlotOffsetMapper slotOffsetMapper) {
    SlotInfo slotInfo = new SlotInfo(slotType, slotOffsetMapper);
    slotInfos.add(slotInfo);
    staticSlotsSize.getAndAdd(slotType.getStaticSlotsSize());
    slotInfoMap.put(slot, slotInfo);
  }

  public SlotInfo getSlotInfo(Slot slot) {
    return slotInfoMap.get(slot);
  }

  public int getStaticSlotsSize() {
    return staticSlotsSize.get();
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
  private Deque<SlotInfo> getMergedSlotInfoDequeue() {
    Struct node = parent;
    Deque<SlotInfo> mergedSlotInfoDeque = new ArrayDeque<>();

    slotInfos.descendingIterator().forEachRemaining(mergedSlotInfoDeque::addLast);

    while (node != null) {
      node.slotInfos.descendingIterator().forEachRemaining(mergedSlotInfoDeque::addLast);
      node = node.getParent();
    }
    return mergedSlotInfoDeque;
  }

  /**
   * SlotInfo contains the type and a mapper to compute the offset from a base address
   */
  public static class SlotInfo {
    private final SlotType slotType;
    private final SlotOffsetMapper slotOffsetMapper;

    public SlotInfo(SlotType slotType, SlotOffsetMapper slotOffsetMapper) {
      this.slotType = slotType;
      this.slotOffsetMapper = slotOffsetMapper;
    }

    public SlotType getSlotType() {
      return slotType;
    }

    public SlotOffsetMapper getSlotOffsetMapper() {
      return slotOffsetMapper;
    }

    public int getOffset(long address) {
      return slotOffsetMapper.getSlotStartOffset(address);
    }
  }
}
