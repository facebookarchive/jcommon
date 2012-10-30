package com.facebook.config.dynamic;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestStringOptions {
  private StringOptions options;
  private Option<String> option1;
  private Option<String> option2;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    options = new StringOptions();
    option1 = options.getOption("test_option");
    option2 = options.getOption("test_option");
  }

  @Test(groups = "fast")
  public void testSameOptionUpdates() throws Exception {
    Assert.assertNull(option1.getValue());
    Assert.assertNull(option2.getValue());
    option1.setValue("foofoo");
    Assert.assertEquals(option1.getValue(), "foofoo");
    Assert.assertEquals(option2.getValue(), "foofoo");
  }

  @Test(groups = "fast")
  public void testSetOptionPropgates() throws Exception {
    Assert.assertNull(option1.getValue());
    Assert.assertNull(option2.getValue());
    options.setOption("test_option", "foofoo");
    Assert.assertEquals(option1.getValue(), "foofoo");
    Assert.assertEquals(option2.getValue(), "foofoo");
  }
}
