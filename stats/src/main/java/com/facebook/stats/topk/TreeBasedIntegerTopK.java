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
package com.facebook.stats.topk;

import com.facebook.collections.ComparablePair;
import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/*
 * Tree-based implementation of integer key streaming top-k.
 * This version is optimized for fast retrieval of the top k elements.
 * Time complexity of add() is O(log k) worst case, O(1) best case.
 * Time complexity of getTopK() is O(k).
 * Space usage is O(n + k).
 * (n = keySpaceSize)
 */
public class TreeBasedIntegerTopK implements TopK<Integer> {
  private final int k;
  private final long[] counts;
  private final boolean[] isInTop;
  private final TreeSet<ComparablePair<Long, Integer>> topPairs = new TreeSet<>();
  private long smallestTopCount = Long.MAX_VALUE;

  public TreeBasedIntegerTopK(int keySpaceSize, int k) {
    this.k = k;
    counts = new long[keySpaceSize];
    isInTop = new boolean[keySpaceSize];
  }

  /*
   * Cost of an add() operation is O(log k) if the candidate has to be inserted in
   * the current top-k elements, O(1) otherwise.
   */
  @Override
  public synchronized void add(Integer key, long count) {
    Preconditions.checkNotNull(key, "key can't be null");
    Preconditions.checkElementIndex(key, counts.length, "key");
    Preconditions.checkArgument(count >= 0, "count to add must be non-negative, got %s", count);

    if (count == 0) {
      return;
    }

    long currentCount = counts[key];

    counts[key] += count;

    if (isInTop[key]) {
      topPairs.remove(new ComparablePair<>(currentCount, key));
      topPairs.add(new ComparablePair<>(counts[key], key));
    } else if (topPairs.size() < k) {
      topPairs.add(new ComparablePair<>(counts[key], key));
      isInTop[key] = true;
      smallestTopCount = Math.min(smallestTopCount, counts[key]);
    } else if (counts[key] > smallestTopCount) {
      ComparablePair<Long, Integer> smallestTopPair = topPairs.pollFirst();

      isInTop[smallestTopPair.getSecond()] = false;
      topPairs.add(new ComparablePair<>(counts[key], key));
      isInTop[key] = true;
      smallestTopCount = topPairs.first().getFirst();
    }
  }

  @Override
  public synchronized List<Integer> getTopK() {
    LinkedList<Integer> topK = new LinkedList<>();

    for (ComparablePair<Long, Integer> pair : topPairs) {
      topK.addFirst(pair.getSecond());
    }

    return topK;
  }
}
