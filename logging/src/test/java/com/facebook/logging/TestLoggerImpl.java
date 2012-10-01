package com.facebook.logging;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestLoggerImpl {
  private static final Logger LOG = LoggerImpl.getClassLogger();

  @Test(groups = "fast")
  public void testMagicSanity() throws Exception {
    LOG.info("logger created from static scope");

    Assert.assertEquals(LOG.getName(), getClass().getName());
  }

  @Test(groups = "fast")
  public void testLocalLoggerGetsClass() throws Exception {
    Logger privateLogger = LoggerImpl.getClassLogger();

    privateLogger.info("private logger");

    Assert.assertEquals(privateLogger.getName(), getClass().getName());
  }
}
