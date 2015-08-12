package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import com.facebook.memory.data.types.definitions.SlotTypes;
import com.facebook.memory.data.types.definitions.Struct;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;

public class TestOffHeapCacheEntry {

  private OffHeapCacheEntryImpl cacheEntry;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 128 * 1024 * 1024;
    SlabFactory slabFactory = new ManagedSlabFactory();
    slab = slabFactory.create(slabSizeBytes);
    cacheEntry = OffHeapCacheEntryImpl.allocate(slab);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testSlots() throws Exception {
    Struct offHeapCacheEntryStruct = Structs.getStruct(OffHeapCacheEntryImpl.class);
    List<Struct.SlotInfo> slotInfoList = offHeapCacheEntryStruct.getMergedSlotInfoList();

    // next, previous, data
    Assert.assertEquals(slotInfoList.size(), 3);
    Struct.SlotInfo nextSlotInfo = slotInfoList.get(0);
    Struct.SlotInfo dataSlotInfo = slotInfoList.get(1);
    Struct.SlotInfo previousSlotInfo = slotInfoList.get(2);

    Assert.assertEquals(nextSlotInfo.getSlotType(), SlotTypes.ADDRESS);
    Assert.assertEquals(nextSlotInfo.getOffset(0), 0);
    Assert.assertEquals(dataSlotInfo.getSlotType(), SlotTypes.ADDRESS);
    Assert.assertEquals(dataSlotInfo.getOffset(0), 8);
    Assert.assertEquals(previousSlotInfo.getSlotType(), SlotTypes.ADDRESS);
    Assert.assertEquals(previousSlotInfo.getOffset(0), 16);
    Assert.assertEquals(Structs.getStaticSlotSize(OffHeapCacheEntryImpl.class), 24);
  }

  @Test(groups = {"fast", "local"})
  public void testSanity() throws Exception {
    cacheEntry.setDataPointer(500);
    cacheEntry.setPrevious(99);
    cacheEntry.setNext(101);

    Assert.assertEquals(cacheEntry.getPrevious(), 99);
    Assert.assertEquals(cacheEntry.getNext(), 101);
    Assert.assertEquals(cacheEntry.getDataPointer(), 500);
  }
}
