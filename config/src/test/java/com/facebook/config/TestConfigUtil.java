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
package com.facebook.config;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfigUtil {
  @Test(groups = "fast")
  public void testDurationSanity() throws Exception {
    Assert.assertEquals(ConfigUtil.getDurationMillis("61"), 61);
    Assert.assertEquals(ConfigUtil.getDurationMillis("59ms"), 59);
    Assert.assertEquals(ConfigUtil.getDurationMillis("100s"), 100000);
    Assert.assertEquals(ConfigUtil.getDurationMillis("16m"), 960000);
    Assert.assertEquals(ConfigUtil.getDurationMillis("1h"), 3600000);
    Assert.assertEquals(ConfigUtil.getDurationMillis("2h"), 7200000);
    Assert.assertEquals(ConfigUtil.getDurationMillis("1d"), 86400000);
    Assert.assertEquals(ConfigUtil.getDurationMillis("30d"), 2592000000L);
    Assert.assertEquals(ConfigUtil.getDurationMillis("2y"), 63072000000L);
  }

  @Test(groups = "fast")
  public void testSizeSanity() throws Exception {
    Assert.assertEquals(ConfigUtil.getSizeBytes("1001"), 1001);
    Assert.assertEquals(ConfigUtil.getSizeBytes("101b"), 101);
    Assert.assertEquals(ConfigUtil.getSizeBytes("10k"), 10240);
    Assert.assertEquals(ConfigUtil.getSizeBytes("101m"), 105906176);
    Assert.assertEquals(ConfigUtil.getSizeBytes("2g"), 2147483648L);
  }
}
