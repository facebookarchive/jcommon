package com.facebook.memory.slabs;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import com.facebook.memory.Sizes;

public class TestMostFreeSpaceAllocationPolicy {

  private MostFreeSpaceAllocationPolicy mostFreeSpaceAllocationPolicy;
  private SlabPool slabPool;
  private List<Slab> slabList;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    ManagedSlabFactory slabFactory = new ManagedSlabFactory();
    slabList = ImmutableList.<Slab>builder()
      .add(slabFactory.create(Sizes.MB.ov(32)))
      .add(slabFactory.create(Sizes.MB.ov(16)))
      .add(slabFactory.create(Sizes.MB.ov(8)))
      .build();

    slabPool = new SlabPoolImpl(slabList);
    mostFreeSpaceAllocationPolicy = new MostFreeSpaceAllocationPolicy(slabPool);
  }

  @Test
  public void testAllocate() throws Exception {
    mostFreeSpaceAllocationPolicy.allocate(slab -> slab.allocate(Sizes.MB.ov(31)));
    // slab 0 should have had the most free memory, so it should have been allocated from
    Assert.assertEquals(slabList.get(0).getFree(), Sizes.MB.ov(1));
    mostFreeSpaceAllocationPolicy.allocate(slab -> slab.allocate(Sizes.MB.ov(14)));
    // now slab 1
    Assert.assertEquals(slabList.get(0).getFree(), Sizes.MB.ov(1));
    Assert.assertEquals(slabList.get(1).getFree(), Sizes.MB.ov(2));
    // now slab 2
    mostFreeSpaceAllocationPolicy.allocate(slab -> slab.allocate(Sizes.MB.ov(5)));
    Assert.assertEquals(slabList.get(0).getFree(), Sizes.MB.ov(1));
    Assert.assertEquals(slabList.get(1).getFree(), Sizes.MB.ov(2));
    Assert.assertEquals(slabList.get(2).getFree(), Sizes.MB.ov(3));
    // now slab 0 - 1mb, slab 1 - 2mb, slab 2 - 3mb, so slab 2 receives the request
    mostFreeSpaceAllocationPolicy.allocate(slab -> slab.allocate(Sizes.MB.ov(1)));
    Assert.assertEquals(slabList.get(0).getFree(), Sizes.MB.ov(1));
    Assert.assertEquals(slabList.get(1).getFree(), Sizes.MB.ov(2));
    Assert.assertEquals(slabList.get(2).getFree(), Sizes.MB.ov(2));
  }

  private static class SlabPoolImpl implements SlabPool {
    private final List<Slab> slabList;

    private SlabPoolImpl(List<Slab> slabList) {
      this.slabList = slabList;
    }

    @Override
    public Slab getSlabByAddress(long address) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Slab getSlabByIndex(int index) {
      return null;
    }

    @Override
    public void freeSlabPool() {
      throw new UnsupportedOperationException();

    }

    @Override
    public int getSize() {
      return slabList.size();
    }

    @Override
    public Iterator<Slab> iterator() {
      return slabList.iterator();
    }
  }
}
