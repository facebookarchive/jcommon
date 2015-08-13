package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import com.facebook.memory.Sizes;
import com.facebook.memory.data.types.definitions.FieldOffsetMapper;
import com.facebook.memory.data.types.definitions.Struct;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;
import com.facebook.memory.slabs.ThreadLocalSlabFactory;

public class TestOffHeapCacheEntryImpl {

  private OffHeapCacheEntry offHeapCacheEntry;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ThreadLocalSlabFactory(Sizes.MB.ov(1), new ManagedSlabFactory());

    slab = slabFactory.create(Sizes.MB.ov(32));
    offHeapCacheEntry = OffHeapCacheEntryImpl.allocate(slab);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testSlots() throws Exception {
    Struct struct = Structs.getStruct(OffHeapCacheEntryImpl.class);
    List<Struct.Field> mergedFieldList = struct.getMergedFieldList();

    Assert.assertEquals(mergedFieldList.size(), 3);
    checkField(mergedFieldList.get(0), 0, 8);
    checkField(mergedFieldList.get(1), 8, 8);
    checkField(mergedFieldList.get(2), 16, 8);
  }


  private void checkField(Struct.Field field, int offset, int size) {
    FieldOffsetMapper fieldOffsetMapper = field.getFieldOffsetMapper();
    Assert.assertEquals(fieldOffsetMapper.getFieldStartOffset(0), offset);
    Assert.assertEquals(fieldOffsetMapper.getFieldSize(0, 0), size);

  }

  @Test
  public void testReadWrite() throws Exception {
    offHeapCacheEntry.setDataPointer(101L);
    offHeapCacheEntry.setNext(102L);
    offHeapCacheEntry.setPrevious(103L);

    Assert.assertEquals(offHeapCacheEntry.getDataPointer(), 101L);
    Assert.assertEquals(offHeapCacheEntry.getNext(), 102L);
    Assert.assertEquals(offHeapCacheEntry.getPrevious(), 103L);
  }
}
