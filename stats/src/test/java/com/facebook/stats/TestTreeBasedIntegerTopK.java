package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

public class TestTreeBasedIntegerTopK extends TestIntegerTopK {
  private static final Logger LOG = LoggerImpl.getLogger(TestTreeBasedIntegerTopK.class);

  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new TreeBasedIntegerTopK(keySpaceSize, k);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }
}
