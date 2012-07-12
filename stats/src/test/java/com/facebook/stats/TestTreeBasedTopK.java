package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

public class TestTreeBasedTopK extends TestIntegerTopK {
  private static final Logger LOG = LoggerImpl.getLogger(TestTreeBasedTopK.class);

  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new TreeBasedTopK(k);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }
}
