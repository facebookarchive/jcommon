package com.facebook.collections.specialized;

import com.facebook.util.digest.LongMurmur3Hash;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestLongMurmur3Hash {

  @Test(groups = "fast")
  public void testConsistency() throws Exception {
    LongMurmur3Hash longMurmur3Hash1 = new LongMurmur3Hash();
    LongMurmur3Hash longMurmur3Hash2 = new LongMurmur3Hash();

    for (long i = 0; i < 1000; i++) {
      Assert.assertEquals(
        longMurmur3Hash1.computeDigest(i),
        longMurmur3Hash2.computeDigest(i),
        String.format("Hashes don't match for %d", i)
      );
    }
  }
}
