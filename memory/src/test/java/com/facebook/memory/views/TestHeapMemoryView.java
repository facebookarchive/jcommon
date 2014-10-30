package com.facebook.memory.views;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestHeapMemoryView {

  private HeapMemoryView memoryView;
  private byte[] bytes;
  private int[] indices;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    bytes = new byte[1024];
    memoryView = new HeapMemoryView(bytes, 0);
    indices = new int[] {
      0, Long.BYTES, 2*Long.BYTES
    };
  }

  @Test(groups = "fast")
  public void testGetPutLong() throws Exception {
    memoryView.putLong(indices[0], 100L);
    Assert.assertEquals(memoryView.getLong(indices[0]), 100L);
    memoryView.putLong(indices[1], Long.MIN_VALUE);
    Assert.assertEquals(memoryView.getLong(indices[1]), Long.MIN_VALUE);
    memoryView.putLong(indices[2], Long.MAX_VALUE);
    Assert.assertEquals(memoryView.getLong(indices[2]), Long.MAX_VALUE);
  }
}