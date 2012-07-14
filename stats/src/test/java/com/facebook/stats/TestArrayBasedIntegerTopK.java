package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.topk.ArrayBasedIntegerTopK;
import com.facebook.stats.topk.TopK;

public class TestArrayBasedIntegerTopK extends TestIntegerTopK {
  private static final Logger LOG = LoggerImpl.getLogger(TestArrayBasedIntegerTopK.class);

  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new ArrayBasedIntegerTopK(keySpaceSize, k);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }
}
