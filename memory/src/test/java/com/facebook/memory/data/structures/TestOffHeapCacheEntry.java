package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import com.facebook.memory.data.types.definitions.FieldType;
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
    List<Struct.Field> fieldList = offHeapCacheEntryStruct.getMergedFieldList();

    // next, previous, data
    Assert.assertEquals(fieldList.size(), 3);
    Struct.Field nextField = fieldList.get(0);
    Struct.Field dataField = fieldList.get(1);
    Struct.Field previousField = fieldList.get(2);

    Assert.assertEquals(nextField.getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(nextField.getOffset(0), 0);
    Assert.assertEquals(dataField.getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(dataField.getOffset(0), 8);
    Assert.assertEquals(previousField.getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(previousField.getOffset(0), 16);
    Assert.assertEquals(Structs.getStaticFieldSize(OffHeapCacheEntryImpl.class), 24);
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
