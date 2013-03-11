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
package com.facebook.data.types;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestStringDatum {
  private static final Logger LOG = Logger.getLogger(TestStringDatum.class);
  
  private StringDatum emptyStringDatum;
  private StringDatum trueStringDatum1;
  private StringDatum trueStringDatum2;
  private StringDatum falseStringDatum1;
  private StringDatum falseStringDatum2;
  private StringDatum intStringDatum;
  private StringDatum longStringDatum;
  private StringDatum stringDatum1;
  private StringDatum floatStringDatum;
  private StringDatum stringDatum2;
  private StringDatum stringDatum3;
  private StringDatum zeroDatum;
  private StringDatum negativeZeroDatum;
  private StringDatum multipleDigitsZeroDatum;
  private StringDatum floatingZeroDatum;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    emptyStringDatum = new StringDatum("");
    trueStringDatum1 = new StringDatum("1");
    trueStringDatum2 = new StringDatum("true");
    falseStringDatum1 = new StringDatum("false");
    falseStringDatum2 = new StringDatum("False");
    intStringDatum = new StringDatum("100000");
    longStringDatum = new StringDatum("10000000000");
    stringDatum1 = new StringDatum("a string");
    stringDatum2 = new StringDatum("a string");
    stringDatum3 = new StringDatum("b string");
    floatStringDatum = new StringDatum("1.1");
    zeroDatum = new StringDatum("0");
    negativeZeroDatum = new StringDatum("-0");
    multipleDigitsZeroDatum = new StringDatum("0000");
    floatingZeroDatum = new StringDatum("0.0");
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(emptyStringDatum.asBoolean(), false);
    Assert.assertEquals(trueStringDatum1.asBoolean(), true);
    Assert.assertEquals(trueStringDatum2.asBoolean(), true);
    Assert.assertEquals(falseStringDatum1.asBoolean(), false);
    Assert.assertEquals(falseStringDatum2.asBoolean(), false);
    Assert.assertEquals(intStringDatum.asInteger(), 100000);
    Assert.assertEquals(longStringDatum.asLong(), 10000000000L);
    Assert.assertEquals(stringDatum1.asBoolean(), true);
    Assert.assertEquals(Float.compare(floatStringDatum.asFloat(), 1.1f), 0);
    Assert.assertFalse(zeroDatum.asBoolean());
    Assert.assertFalse(negativeZeroDatum.asBoolean());
    Assert.assertFalse(multipleDigitsZeroDatum.asBoolean());
    // TODO should 0.0 be false?
    Assert.assertTrue(floatingZeroDatum.asBoolean());
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(stringDatum1.compareTo(stringDatum3), -1);
    Assert.assertEquals(stringDatum1.compareTo(stringDatum1), 0);
    Assert.assertEquals(stringDatum1.compareTo(stringDatum2), 0);
    Assert.assertEquals(stringDatum3.compareTo(stringDatum1), 1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(stringDatum1, stringDatum1);    
    Assert.assertEquals(stringDatum1, stringDatum2);    
  }
  
  @Test(groups = "fast")
  public void testError() throws Exception {
    try {
      stringDatum1.asByte();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }

    try {
      stringDatum1.asShort();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }

    try {
      stringDatum1.asInteger();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }

    try {
      stringDatum1.asLong();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }

    try {
      stringDatum1.asFloat();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }

    try {
      stringDatum1.asDouble();
    } catch (NumberFormatException e) {
      LOG.info("got expected exception");
    }
  }
}