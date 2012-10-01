package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.topk.HashBasedTopK;
import com.facebook.stats.topk.TopK;

public class TestHashBasedTopK extends TestIntegerTopK {
  private static final Logger LOG = LoggerImpl.getLogger(TestHashBasedTopK.class);

  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new HashBasedTopK(k);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }
}
