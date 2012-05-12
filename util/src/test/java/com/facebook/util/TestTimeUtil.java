package com.facebook.util;

import com.facebook.util.TimeUtil;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTimeUtil {

  @Test(groups = "fast")
  public void testGetDateTimeZone() throws Exception {
    Assert.assertEquals(TimeUtil.getDateTimeZone(null), DateTimeZone.UTC);
    Assert.assertEquals(TimeUtil.getDateTimeZone(""), DateTimeZone.UTC);
  }

}