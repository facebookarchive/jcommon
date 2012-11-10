package com.facebook.collections.specialized;

import com.facebook.util.digest.DigestFunction;
import com.facebook.util.digest.LongMurmur3Hash;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

public class TestSampledSetImpl {

  private int maxSetSize;
  private SampledSet<Long> integerSet;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    // needs to be a multiple of 4
    maxSetSize = 8;
    DigestFunction<Long> longMurmur3Hash = new LongMurmur3Hash();
    integerSet = new SampledSetImpl<Long>(
      maxSetSize, longMurmur3Hash, new IntegerHashSetFactory(maxSetSize)
    );
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    int largeMaxSetSize = 4000;
    IntegerHashSetFactory intHashSetFactory = new IntegerHashSetFactory(largeMaxSetSize);
    SampledSet<Long> largeSet =
      new SampledSetImpl<Long>(largeMaxSetSize, new LongMurmur3Hash(), intHashSetFactory);
    int numElements = 2000 * largeMaxSetSize;

    for (int i = 0; i < numElements; i++) {
      largeSet.add((long)i);
    }

    assertValidMaxSize();
    
    float error = 
      Math.abs(largeSet.getScaledSize() - numElements) / (float) numElements;
    String message = String.format(
      "actual: %d estimate: %d error: %f",
      numElements,
      largeSet.getScaledSize(),
      error
    );
    // this test case is deterministic and we expect less than 2-3%
    Assert.assertTrue(error < 0.02, message);
  }
  
  @Test(groups = "fast")
  public void testAddDuplicateKeys() throws Exception {
    // add the set to full
    Set<Integer> elements = new HashSet<Integer>(maxSetSize);
    for (int i = 0; i < 100000; i += 2) { // just find 8 elements
      if (integerSet.add((long)i)) {
        elements.add(i);

        if (elements.size() == maxSetSize) {
          break;
        }
      }
    }
    Assert.assertEquals(integerSet.size(), maxSetSize);
    Assert.assertEquals(integerSet.getScaledSize(), maxSetSize);

    SampledSet<Long> setCopy = integerSet.makeSnapshot();
    System.err.println("");
    // add those elements again, down sampling should not happen
    for (Integer i : elements) {
      if (integerSet.add((long) i)) {
        System.err.println("");
      }
    }

    Assert.assertEquals(setCopy.getEntries(), integerSet.getEntries());
//    Assert.assertEquals(integerSet.size(), maxSetSize);
//    Assert.assertEquals(integerSet.getScaledSize(), maxSetSize);
  }

  private void assertValidMaxSize() {
    Assert.assertTrue(
      integerSet.getSize() <= maxSetSize,
      String.format(
        "max size %d exceeded at %d", maxSetSize, integerSet.getSize()
      )
    );
  }

  @Test(groups = "fast")
  public void testSetSizeBelowMax() throws Exception {
    int numElements = maxSetSize;

    for (int i = 0; i < numElements; i++) {
      integerSet.add((long)i);
    }

    Assert.assertEquals(integerSet.getScaledSize(), numElements);
  }
  
  @Test(groups = "fast")
  public void testMaxNeverExceeded() throws Exception {
    int numElements = 10 * maxSetSize;

    for (int i = 0; i < numElements; i++) {
      integerSet.add((long)i);
      assertValidMaxSize();
    }
  }

  @Test(groups = "fast")
  public void testMerge() throws Exception {
    IntegerHashSetFactory longHashSetFactory = new IntegerHashSetFactory(maxSetSize);
    SampledSet<Long> otherSet =
      new SampledSetImpl<Long>(maxSetSize / 4, new LongMurmur3Hash(), longHashSetFactory);
    int firstSetSize = maxSetSize / 2;
    
    // populate the first set to its max size
    for (int i = 0; i < firstSetSize; i++) {
      integerSet.add((long)i);
    }
    
    Assert.assertEquals(integerSet.getScaledSize(), firstSetSize);
    
    int secondSetSize = maxSetSize / 4;
    
    // populate the second set to its max size
    for (int i = 0; i < secondSetSize; i++) {
      otherSet.add((long)i);
    }
    
    Assert.assertEquals(otherSet.getScaledSize(), secondSetSize);
    
    // merge into the first
    SampledSet<Long> mergeIntoFirst = integerSet.merge(otherSet);
    // merge into the second
    SampledSet<Long> mergeIntoSecond = otherSet.merge(integerSet);
    
    // make sure we take the max of the maxSetSizes
    int maxofMaxSetSize = 
      Math.max(integerSet.getMaxSetSize(), otherSet.getMaxSetSize());
    Assert.assertEquals(
      mergeIntoFirst.getMaxSetSize(),
      maxofMaxSetSize
    );
    Assert.assertTrue(mergeIntoFirst.getSize() < maxofMaxSetSize);
    
    // same if we merge into the second set
    Assert.assertEquals(mergeIntoSecond.getMaxSetSize(), secondSetSize);
  }
  
  @Test(groups = "fast")
  public void testMergeWithEmpty() throws Exception {
    IntegerHashSetFactory longHashSetFactory = new IntegerHashSetFactory(4);
    DigestFunction<Long> digestFunction = new LongMurmur3Hash();

    SampledSet<Long> set1 =
      new SampledSetImpl<Long>(4, digestFunction, longHashSetFactory);
  	SampledSet<Long> set2 = 
      new SampledSetImpl<Long>(4, digestFunction, longHashSetFactory);
  	
    set1.add(1L);
    set1.add(2L);
    set1.add(3L);
    set1.add(4L);
    set1.add(5L);

    SampledSet<Long> merged = set1.merge(set2);

    Assert.assertEquals(merged.getScaledSize(), set1.getScaledSize());
  }
  
  @Test(groups = "fast")
  public void testMergeWithDownSample() throws Exception {
    IntegerHashSetFactory intHashSetFactory = new IntegerHashSetFactory(4);
    SampledSet<Long> set1 = 
      new SampledSetImpl<Long>(2, LongMurmur3Hash.getInstance(), intHashSetFactory);
    SampledSet<Long> set2 = 
      new SampledSetImpl<Long>(3, LongMurmur3Hash.getInstance(), intHashSetFactory);
    
    // set 1 will have 1 element and sample rate of 1
    set1.add(1L);
    // set 2 will have 2 elements and sample rate of 2
    set2.add(2L);
    set2.add(4L);
    set2.add(8L);
    set2.add(16L);
    // scaled sizes are 1 and 4
    Assert.assertEquals(set1.getScaledSize(), 1);
    Assert.assertEquals(set2.getScaledSize(), 6);
    
    // now merge: should result in a set with a max size of 2 and a sample 
    // rate of 2
    SampledSet<Long> merge1with2 = set1.merge(set2);
    SampledSet<Long> merge2with1 = set2.merge(set1);
    
    // merge is NOT symmetric as the higher sample rate must be used
    Assert.assertEquals(merge1with2.getScaledSize(), 4);
    Assert.assertEquals(merge2with1.getScaledSize(), 6);
  }
  
  @Test(groups = "fast")
  public void testHasChanged() throws Exception {
    // initial set should not indicate it has changed
  	Assert.assertFalse(integerSet.hasChanged());
    // newly constructed set from merge() should also return false
  	Assert.assertFalse(integerSet.merge(integerSet).hasChanged());
  }

  @Test(groups = "fast")
  public void testProposedSize() throws Exception {
    SampledSet<Long> set1 =
      new SampledSetImpl<Long>(8, new LongMurmur3Hash(), new IntegerHashSetFactory());
    SampledSet<Long> set2 =
      new SampledSetImpl<Long>(8, new LongMurmur3Hash(), new IntegerHashSetFactory());
    SampledSet<Long> set3 =
      new SampledSetImpl<Long>(8, new LongMurmur3Hash(), new IntegerHashSetFactory());

    // set 1 will have 4 elements and sample rate of 1
    set1.add(0L);
    set1.add(1L);
    set1.add(2L);
    set1.add(3L);
    // set 2 will have 4 elements and sample rate of 1
    set2.add(4L);
    set2.add(5L);
    set2.add(6L);
    set2.add(7L);
    // set 3 will have 1 elements and sample rate of 1
    set3.add(9L);
    // scaled sizes are 1 and 4
    Assert.assertEquals(set1.getScaledSize(), 4);
    Assert.assertEquals(set2.getScaledSize(), 4);
    Assert.assertEquals(set3.getScaledSize(), 1);

    // now merge:
    // note that making snapshot should not change "proposedSize"
    SampledSet<Long> merged = set1.makeSnapshot();
    merged.mergeInPlaceWith(set2.makeTransientSnapshot());
    merged = merged.makeTransientSnapshot();
    // at this point, merged is a full set. We will add one more element to it.
    merged.mergeInPlaceWith(set3);
    // now merged should be down sampled
    Assert.assertEquals(merged.getSize(), 6);
    Assert.assertEquals(merged.getScaledSize(), 12);
  }
}
