package com.facebook.memory.data.types.definitions;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.Slabs;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;
import com.facebook.memory.views.ReadableMemoryView;

public class TestSpanNode {

  private Slab slab1;
  private Slab slab2;
  private int sizeBytes;
  private MemoryViewController memoryViewController1;
  private MemoryViewController memoryViewController2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    sizeBytes = 512 * 1024 * 1024;
    slab1 = Slabs.newManagedSlab(sizeBytes);
    slab2 = Slabs.newManagedSlab(sizeBytes);
    memoryViewController1 = new MemoryViewController(MemoryView32.factory(), slab1);
    memoryViewController2 = new MemoryViewController(MemoryView32.factory(), slab2);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab1.freeSlab();
    slab2.freeSlab();
  }

  @Test(groups = "fast")
  public void testLinkedList() throws Exception {
    MemoryView head = SpanNode.DEF.allocate(memoryViewController1);
    MemoryView data = memoryViewController2.allocate(Long.BYTES);

    data.nextLong(0);
    SpanNode spanNode = SpanNode.DEF;
    Span span = spanNode.span();
    Node node = spanNode.node();

    spanNode.span().dataAddress().set(data.getAddress(), head);
    spanNode.span().size().set(Integer.BYTES, head);
    spanNode.node().next().set(MemoryConstants.NO_ADDRESS, head);
    spanNode.node().previous().set(MemoryConstants.NO_ADDRESS, head);

    MemoryView current = head;
    int numIter = 4096;

    for (int i = 1; i < numIter; i++) {
//      dataPtr = slab2.allocate(Long.SIZE);
      data = memoryViewController1.allocate(Long.SIZE);
      data.putLong(0, i*i);
      MemoryView next = spanNode.allocate(memoryViewController2);

      spanNode.span().dataAddress().set(data.getAddress(), next);
      span.size().set(Integer.BYTES, next);
      node.next().set(next.getAddress(), current);
      node.previous().set(current.getAddress(), next);
      current = next;
    }

    ReadableMemoryView tail = current;
    ReadableMemoryView ptr = head;
    int cnt = 0;
    // walk list forward
    while (ptr.getAddress() != MemoryConstants.NO_ADDRESS) {
//      System.err.println(SpanNode.DATA_ADDRESS.get(ptr));
//      System.err.println(SpanNode.SIZE.get(ptr));
//      System.err.println(SpanNode.NEXT.get(ptr));
//      System.err.println(SpanNode.PREVIOUS.get(ptr));
      MemoryView dataView = MemoryView32.factory().wrap(span.dataAddress().get(ptr), Long.BYTES);
      Assert.assertEquals(dataView.nextLong(), cnt * cnt);
//      System.err.println(dataView.nextLong());

      long nextPtr = node.next().get(ptr);

//      System.err.println("--");
      ptr = new MemoryView32(nextPtr, spanNode.getSize());
      cnt++;
    }


    ptr = tail;
    //walk list backwards
    while (ptr.getAddress() != MemoryConstants.NO_ADDRESS) {
      cnt--;
      MemoryView dataView = MemoryView32.factory().wrap(span.dataAddress().get(ptr), Long.BYTES);
      Assert.assertEquals(dataView.nextLong(), cnt * cnt);

      long nextPtr = node.previous().get(ptr);

      ptr = new MemoryView32(nextPtr, spanNode.getSize());
    }

    Assert.assertEquals(cnt, 0);
  }
}
