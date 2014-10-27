package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.structures.FreeList;
import com.facebook.memory.data.structures.IntRange;
import com.facebook.memory.data.structures.Ranges;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewMediator;

public class TestManagedSlab {
  private ManagedSlab managedSlab1;
  private ManagedSlab managedSlab5;
  private long baseAddress1;
  private long baseAddress5;
  private MemoryViewMediator memoryViewMediator1;
  private MemoryViewMediator memoryViewMediator5;
  private int memoryViewSize;
  private int managedSlabSize;
  private int numIter;
  private int smallSlabSize;
  private ManagedSlab smallSlab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    long rawSlabSize = 5L * 1024L * 1024L * 1024L;
    OffHeapSlab offHeapSlab = new OffHeapSlab(rawSlabSize);
    numIter = 100;

    managedSlabSize = 1024 * 1024 * 1024;
    smallSlabSize = 64 * 1024 * 1024;
    managedSlab1 = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, managedSlabSize);
    managedSlab5 = new ManagedSlab(offHeapSlab.getBaseAddress() + 4 * managedSlabSize, offHeapSlab, managedSlabSize);
    smallSlab = new ManagedSlab(offHeapSlab.getBaseAddress() + managedSlabSize, offHeapSlab, smallSlabSize);
    baseAddress1 = managedSlab1.getBaseAddress();
    baseAddress5 = managedSlab5.getBaseAddress();
    memoryViewMediator1 = new MemoryViewMediator(managedSlab1);
    memoryViewMediator5 = new MemoryViewMediator(managedSlab5);
    memoryViewSize = 100 * 1024 * 1024;
  }

  @Test(groups = "fast")
  public void testBaseAddress1() throws Exception {
    System.err.println(String.format("got % d bytes at address % d", managedSlabSize, baseAddress1));

    Assert.assertTrue(baseAddress1 > 0);
  }

  @Test(groups = "fast")
  public void testBaseAddress5() throws Exception {
    System.err.println(String.format("got % d bytes at address % d", managedSlabSize, baseAddress5));

    Assert.assertTrue(baseAddress5 > 0);
  }

  @Test(groups = "fast")
  public void testPutByte1() throws Exception {
    managedSlab1.putByte(baseAddress1, (byte) Byte.MAX_VALUE);
    Assert.assertEquals(managedSlab1.getByte(baseAddress1), (byte) Byte.MAX_VALUE);
  }

  @Test(groups = "fast")
  public void testPutByte5() throws Exception {
    managedSlab5.putByte(baseAddress5, (byte) Byte.MIN_VALUE);
    Assert.assertEquals(managedSlab5.getByte(baseAddress5), (byte) Byte.MIN_VALUE);
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewByte1() throws Exception {
    MemoryView memoryView = memoryViewMediator1.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i++) {
      memoryView.putByte(i, (byte) (i % Byte.MAX_VALUE));
    }

    MemoryView reset = memoryViewMediator1.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i++) {
      Assert.assertEquals(reset.nextByte(), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewByte5() throws Exception {
    MemoryView memoryView = memoryViewMediator5.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i++) {
      memoryView.putByte(i, (byte) (i % Byte.MAX_VALUE));
    }

    MemoryView reset = memoryViewMediator5.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i++) {
      Assert.assertEquals(reset.nextByte(), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewInt1() throws Exception {
    MemoryView memoryView = memoryViewMediator1.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i += Integer.BYTES) {
      memoryView.putInt(i, i % Integer.MAX_VALUE);
    }

    MemoryView reset = memoryViewMediator1.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i += Integer.BYTES) {
      Assert.assertEquals(reset.nextInt(), i % Integer.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewInt5() throws Exception {
    MemoryView memoryView = memoryViewMediator5.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i += Integer.BYTES) {
      memoryView.putInt(i, i % Integer.MAX_VALUE);
    }

    MemoryView reset = memoryViewMediator5.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i += Integer.BYTES) {
      Assert.assertEquals(reset.nextInt(), i % Integer.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewLong1() throws Exception {
    MemoryView memoryView = memoryViewMediator1.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i += Long.BYTES) {
      memoryView.putInt(i, i);
    }

    MemoryView reset = memoryViewMediator1.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i += Long.BYTES) {
      Assert.assertEquals(reset.nextLong(), i);
    }
  }

  @Test(groups = "fast")
  public void testPutViaMemoryViewLong5() throws Exception {
    MemoryView memoryView = memoryViewMediator5.allocate32(memoryViewSize);

    for (int i = 0; i < memoryViewSize; i += Long.BYTES) {
      memoryView.putInt(i, i);
    }

    MemoryView reset = memoryViewMediator5.reset(memoryView);

    for (int i = 0; i < memoryViewSize; i += Long.BYTES) {
      Assert.assertEquals(reset.nextLong(), i);
    }
  }

  @Test(groups = "fast")
  public void testGetByte() throws Exception {
    long address = managedSlab1.getBaseAddress();

    for (int i = 0; i < memoryViewSize; i++) {
      managedSlab1.putByte(address + i, (byte) (i % Byte.MAX_VALUE));
    }

    for (int i = 0; i < memoryViewSize; i++) {
      Assert.assertEquals(managedSlab1.getByte(address + i), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testGet() throws Exception {
    long address = managedSlab1.getBaseAddress();

    for (int i = 0; i < memoryViewSize; i++) {
      managedSlab1.putByte(address + i, (byte) (i % Byte.MAX_VALUE));
    }

    MemoryView memoryView = managedSlab1.get(address, memoryViewSize);

    for (int i = 0; i < memoryViewSize; i++) {
      Assert.assertEquals(memoryView.nextByte(), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testAllocateAndFree() throws Exception {
    List<Long> addressList = new ArrayList<>();
    int mb = 1024 * 1024;
    int numAllocs = 64;

    for (int i = 0; i < numAllocs; i++){
      addressList.add(smallSlab.allocate(mb));
    }

    for (int i = 0; i < numAllocs; i += 2) {
      smallSlab.free(addressList.get(i), mb);
    }

    FreeList freeList = smallSlab.getFreeList();
    Set<IntRange> freeRanges = freeList.asRangeSet();
    // we have regions of {0, 1mb}, {2mb, 3mb}, {4mb, 5mb} ...
    Assert.assertEquals(freeRanges.size(), 32);

    for (int i = 1; i < numAllocs; i += 2) {
      smallSlab.free(addressList.get(i), mb);
    }

    // now just one large region
    Assert.assertEquals(freeRanges.size(), 1);
    Assert.assertEquals(freeRanges.iterator().next(), Ranges.make(0, mb * numAllocs - 1));
  }

  @Test(groups = "fast", expectedExceptions = FailedAllocationException.class)
  public void testAllocationFailureNoSegment() throws Exception {
      smallSlab.allocate(smallSlabSize + 1);
  }

  @Test(groups = "fast", expectedExceptions = FailedAllocationException.class)
  public void testAllocationFailureExhaust() throws Exception {
      smallSlab.allocate(smallSlabSize);
      smallSlab.allocate(smallSlabSize);
  }
}
