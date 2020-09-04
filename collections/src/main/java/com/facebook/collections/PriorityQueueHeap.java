/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * reference implementation of a SimpleHeap that delegates to a PriorityQueue. This is basic
 * delegation
 *
 * <p>NOTE: NOT thread-safe due to shrink operation
 *
 * @param <T>
 */
public class PriorityQueueHeap<T> implements SimpleHeap<T> {
  // make this volatile in case someone wants to try for some sort of optimistic concurrency
  private volatile PriorityQueue<T> priorityQueue;

  public PriorityQueueHeap(PriorityQueue<T> priorityQueue) {
    this.priorityQueue = priorityQueue;
  }

  @Override
  public T peek() {
    return priorityQueue.peek();
  }

  @Override
  public T poll() {
    return priorityQueue.poll();
  }

  @Override
  public boolean add(T item) {
    return priorityQueue.add(item);
  }

  @Override
  public boolean addAll(Collection<? extends T> items) {
    return priorityQueue.addAll(items);
  }

  @Override
  public int size() {
    return priorityQueue.size();
  }

  /**
   * attempts to reclaim slots, but we can't know how many, so always return 0.
   *
   * @return
   */
  @Override
  public int shrink() {
    PriorityQueue<T> newPriorityQueue = new PriorityQueue<>(priorityQueue);

    priorityQueue = newPriorityQueue;
    // unfortunately, we don't know the # of slots saved, so we still return 0

    return 0;
  }

  @Override
  public SimpleHeap<T> makeCopy() {
    // deep copy
    return new PriorityQueueHeap<>(new PriorityQueue<>(priorityQueue));
  }

  @Override
  public Iterator<T> iterator() {
    return priorityQueue.iterator();
  }
}
