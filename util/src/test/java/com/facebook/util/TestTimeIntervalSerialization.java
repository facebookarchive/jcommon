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
package com.facebook.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestTimeIntervalSerialization {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    MAPPER.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
  }

  @DataProvider(name = "timeintervals")
  public Object[][] intervals() {
    return new Object[][]{
      {TimeInterval.INFINITE},
      {TimeInterval.ZERO},
      {TimeInterval.withMillis(100)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.MILLIS, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.MILLIS, 999)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.SECOND, 59)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.MINUTE, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.HOUR, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.DAY, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.WEEK, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.MONTH, 1)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.MONTH, 12)},
      {TimeInterval.withTypeAndLength(TimeIntervalType.YEAR, 1)},
      {TimeInterval.withMillis(2592000000L)}
    };
  }

  @Test(groups = "fast", dataProvider = "timeintervals")
  public void testSerDe(TimeInterval timeInterval) throws Exception {
    String serialized = MAPPER.writeValueAsString(timeInterval);
    TimeInterval deserialized = MAPPER.readValue(serialized, TimeInterval.class);
    Assert.assertEquals(timeInterval, deserialized);
  }
}
