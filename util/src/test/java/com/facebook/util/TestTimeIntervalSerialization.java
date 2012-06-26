package com.facebook.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestTimeIntervalSerialization {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    MAPPER.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
    MAPPER.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
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
      {TimeInterval.withTypeAndLength(TimeIntervalType.YEAR, 1)}
    };
  }

  @Test(groups = "fast", dataProvider = "timeintervals")
  public void testSerDe(TimeInterval timeInterval) throws Exception {
    String serialized = MAPPER.writeValueAsString(timeInterval);
    TimeInterval deserialized = MAPPER.readValue(serialized, TimeInterval.class);
    Assert.assertEquals(timeInterval, deserialized);
  }
}
