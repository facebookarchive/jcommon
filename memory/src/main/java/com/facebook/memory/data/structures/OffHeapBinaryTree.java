package com.facebook.memory.data.structures;

import sun.misc.Unsafe;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.UnsafeAccessor;

public class OffHeapBinaryTree {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();
  private volatile Node root = null;

  public void add(long dataAddress, int dataSize) {
    long nodePtr = UNSAFE.allocateMemory(Node.size());
    Node node = Node.create(nodePtr, dataAddress, dataSize);

    if (root == null) {
      root = node;
    } else {
      Node current = root;
      boolean done = false;

      while (!done) {
        int cmp = node.compareTo(current);

        if (cmp == -1) {
          if (current.getLeftPtr() == MemoryConstants.NO_ADDRESS) {
            current.setLeftPtr(node.getAddress());
            done = true;
          } else {
            current = current.getLeftNode();
          }
        } else if (cmp == 1) {
          if (current.getRightPtr() == MemoryConstants.NO_ADDRESS) {
            current.setRightPtr(node.getAddress());
            done = true;
          } else {
            current = current.getRightNode();
          }
        } else {// should not happen
          throw new IllegalStateException("two free-lists with same start");
        }
      }
    }

  }

  public void postOrderWalk(NodeVisitor visitor) {

  }

  public void inOrderWalk(NodeVisitor visitor) {

  }

  public void freeTree() {
    postOrderWalk(
      new NodeVisitor() {
        @Override
        public void visit(Node node) {
          UNSAFE.freeMemory(node.getAddress());
        }
      }
    );
  }
}
