package com.facebook.collections.specialized;

import com.facebook.collections.ConcurrentSetMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

public class TestTrackableSetMap {
  private TrackableSetMap<Integer, Integer, Set<Integer>> trackableSetMap;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    trackableSetMap = new TrackableSetMap<Integer, Integer, Set<Integer>>(
      new ConcurrentSetMap<Integer, Integer>()
    );
  }
  
  @Test(groups = "fast")
  public void testSanity() throws Exception {
  	Assert.assertEquals(trackableSetMap.hasChanged(), false);
    trackableSetMap.add(1, 1);
    Assert.assertEquals(trackableSetMap.hasChanged(), true);
    Assert.assertEquals(trackableSetMap.hasChanged(), false);
    trackableSetMap.add(1, 2);
    trackableSetMap.add(2, 3);
    trackableSetMap.add(3, 4);
    Assert.assertEquals(trackableSetMap.hasChanged(), true);
    Assert.assertEquals(trackableSetMap.hasChanged(), false);
  }
}
