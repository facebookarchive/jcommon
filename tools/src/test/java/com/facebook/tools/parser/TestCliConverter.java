/*
 * Copyright (C) 2014 Facebook, Inc.
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
package com.facebook.tools.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class TestCliConverter {
  @Test(groups = "fast")
  public void testBoolean() throws Exception {
    Assert.assertTrue(CliConverter.BOOLEAN.convert("true"));
    Assert.assertTrue(CliConverter.BOOLEAN.convert("TRUE"));
    Assert.assertFalse(CliConverter.BOOLEAN.convert("false"));
    Assert.assertFalse(CliConverter.BOOLEAN.convert("bar"));
    Assert.assertFalse(CliConverter.BOOLEAN.convert(""));
    Assert.assertNull(CliConverter.BOOLEAN.convert(null));
  }

  @Test(groups = "fast")
  public void testRangeList() throws Exception {
    Assert.assertEquals(CliConverter.INT_LIST.convert("1,2,5"), Arrays.asList(1, 2, 5));
    Assert.assertEquals(CliConverter.INT_LIST.convert("0,7-9,13"), Arrays.asList(0, 7, 8, 9, 13));
    Assert.assertNull(CliConverter.INT_LIST.convert(null));
  }
}
