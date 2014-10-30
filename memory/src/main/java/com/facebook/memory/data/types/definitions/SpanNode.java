package com.facebook.memory.data.types.definitions;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryViewController;

public class SpanNode {
  private static final Node NODE = new Node(0);
  private static final Span SPAN = new Span(NODE.getSize());

  public static final SpanNode DEF = new SpanNode(NODE, SPAN);

  private final Node node;
  private final Span span;

  public SpanNode(Node node, Span span) {
    this.node = node;
    this.span = span;
  }

  public Node node() {
    return node;
  }

  public Span span() {
    return span;
  }

  public int getSize() {
    return node.getSize() + span.getSize();
  }

  public MemoryView allocate(MemoryViewController memoryViewController) throws FailedAllocationException {
    MemoryView memoryView = memoryViewController.allocate(getSize());

    return memoryView;
  }
}
