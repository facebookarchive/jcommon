package com.facebook.memory.views;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;

public class TestMemoryView32 {

  private MemoryViewFactory memoryViewFactory;
  private int slabSize;
  private Slab slab;
  private int memoryViewSize;
  private int memoryViewsCount;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    memoryViewSize = 1024 * 1024;
    memoryViewsCount = 128;
    slabSize = memoryViewsCount * memoryViewSize;

    SlabFactory slabFactory = new ManagedSlabFactory();

    slab = slabFactory.create(slabSize);
    memoryViewFactory = MemoryView32.factory();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test(groups = {"fast", "local"})
  public void testSanity() throws Exception {
    ReadableMemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    Assert.assertEquals(memoryView.getSize(), memoryViewSize);
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = {IllegalStateException.class})
  public void testOverrunByte() throws Exception {
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize + 1; i++) {
      memoryView.nextByte();
    }
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = {IllegalStateException.class})
  public void testOverrunShort() throws Exception {
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize / Short.BYTES + 1; i++) {
      memoryView.nextShort();
    }
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = {IllegalStateException.class})
  public void testOverrunInt() throws Exception {
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize / Integer.BYTES + 1; i++) {
      memoryView.nextInt();
    }
  }

  @Test(groups = {"fast", "local"}, expectedExceptions = {IllegalStateException.class})
  public void testOverrunLong() throws Exception {
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize / Long.BYTES + 1; i++) {
      memoryView.nextLong();
    }
  }

  @Test(groups = {"fast", "local"})
  public void testInPlaceWriteByte() throws Exception {
    MemoryView memoryView1 = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize; i++) {
      memoryView1.nextByte((byte) (i % Byte.MAX_VALUE));
    }

    MemoryView memoryView2 = memoryView1.reset();

    for (int i = 0; i < memoryViewSize; i++) {
      Assert.assertEquals(memoryView2.nextByte(), (byte) (i % Byte.MAX_VALUE));
    }
  }

  @Test(groups = {"fast", "local"})
  public void testInPlaceWriteInt() throws Exception {
    MemoryView memoryView1 = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);

    for (int i = 0; i < memoryViewSize / Integer.BYTES; i++) {
      memoryView1.nextInt(i);
    }

    MemoryView memoryView2 = memoryView1.reset();

    for (int i = 0; i < memoryViewSize / Integer.BYTES; i++) {
      Assert.assertEquals(memoryView2.nextInt(), i);
    }
  }

  @Test(groups = {"fast", "local"})
  public void testSpliceSize() throws Exception {
    MemoryView memoryView = memoryViewFactory.wrap(slab.allocate(memoryViewSize), memoryViewSize);
    int sizeInInts = memoryViewSize / Integer.BYTES;

    for (int i = 0; i < sizeInInts; i++) {
      memoryView.putInt(i * Integer.BYTES, i);
    }

    int spliceSize = memoryViewSize / 2;
    MemoryView splicedMemoryView = memoryView.splice(spliceSize);

    Assert.assertEquals(splicedMemoryView.getSize(), spliceSize);

    int count = 0;

    while (splicedMemoryView.hasNextByte()) {
      Assert.assertEquals(splicedMemoryView.nextInt(), spliceSize / Integer.BYTES + count);

      count++;
    }

    int countBytes = count * 4;
    
    Assert.assertEquals(countBytes, spliceSize);
  }
}
