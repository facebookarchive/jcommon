package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewMediator;

public class TestOffHeapSlab {
  private static final Logger LOG = LoggerImpl.getClassLogger();
  private OffHeapSlab slab;
  private long baseAddress;
  private MemoryViewMediator memoryViewMediator;
  private int byteViewSize;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    slab = new OffHeapSlab(Integer.MAX_VALUE);
    baseAddress = slab.getBaseAddress();
    memoryViewMediator = new MemoryViewMediator(new RawSlabAdapter(slab));
    byteViewSize = 100 * 1024 * 1024;
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test(groups = "fast")
  public void testBaseAddress() throws Exception {
    LOG.info("got %d bytes at address %d", Integer.MAX_VALUE, baseAddress);

    Assert.assertTrue(baseAddress > 0);
  }

  @Test(groups = "fast")
  public void testPutByte() throws Exception {
    slab.putByte(baseAddress, (byte) 255);

    Assert.assertEquals(slab.getByte(baseAddress), (byte) 255);
  }

  @Test(groups = "fast")
  public void testPut() throws Exception {
    MemoryView memoryView = memoryViewMediator.allocate32(byteViewSize);

    for (int i = 0; i < byteViewSize; i++) {
      memoryView.putByte(i, (byte) (i % Byte.MAX_VALUE));
    }

    MemoryView reset = memoryViewMediator.reset(memoryView);

    for (int i = 0; i < byteViewSize; i++) {
      Assert.assertEquals(reset.nextByte(), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testGetByte() throws Exception {
    long address = slab.getBaseAddress();

    for (int i = 0; i < byteViewSize; i++) {
      slab.putByte(address + i, (byte) (i % Byte.MAX_VALUE));
    }

    for (int i = 0; i < byteViewSize; i++) {
      Assert.assertEquals(slab.getByte(address + i), i % Byte.MAX_VALUE);
    }
  }

  @Test(groups = "fast")
  public void testGet() throws Exception {
    long address = slab.getBaseAddress();

    for (int i = 0; i < byteViewSize; i++) {
      slab.putByte(address + i, (byte) (i % Byte.MAX_VALUE));
    }

    MemoryView memoryView = slab.get(address, byteViewSize);

    for (int i = 0; i < byteViewSize; i++) {
      Assert.assertEquals(memoryView.nextByte(), i % Byte.MAX_VALUE);
    }
  }
}
