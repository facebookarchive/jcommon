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
package com.facebook.tools.io;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class TestIO {
  @Test(groups = "fast")
  public void testYes() {
    for (String yes : Arrays.asList("y", "yes", "Y", "YES", "Yes", "yES", "\n", "")) {
      MockIO io = new MockIO(yes);
      YesNo response = io.ask(YesNo.YES, "Did the %s test pass", yes);

      Assert.assertEquals(io.getOut(), "Did the " + yes + " test pass [Y/n]? ");
      Assert.assertEquals(io.getErr(), "");
      Assert.assertEquals(response, YesNo.YES, String.format("Input [%s]:", yes));
      Assert.assertTrue(response.isYes());
      Assert.assertFalse(response.isNo());
    }
  }

  @Test(groups = "fast")
  public void testNo() {
    for (String no : Arrays.asList("n", "no", "N", "NO", "No", "nO", "\n", "")) {
      MockIO io = new MockIO(no);
      YesNo response = io.ask(YesNo.NO, "Did the %s test pass", no);

      Assert.assertEquals(io.getOut(), "Did the " + no + " test pass [y/N]? ");
      Assert.assertEquals(io.getErr(), "");
      Assert.assertEquals(response, YesNo.NO, String.format("Input [%s]:", no));
      Assert.assertTrue(response.isNo());
      Assert.assertFalse(response.isYes());
    }
  }

  @Test(groups = "fast")
  public void testReprompt() {
    MockIO io = new MockIO("foo bar\nbaz\nyes\n");
    YesNo response = io.ask(YesNo.NO, "Did the test pass");

    Assert.assertEquals(
      io.getOut(), "Did the test pass [y/N]? Did the test pass [y/N]? Did the test pass [y/N]? "
    );
    Assert.assertEquals(io.getErr(), "");
    Assert.assertEquals(response, YesNo.YES);
  }

  @Test(groups = "fast")
  public void testNotAnnotated() {
    MockIO io = new MockIO();

    try {
      io.ask(NotAnnotated.FOO, "Did the test pass");
      Assert.fail("Expected exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(
        e.getMessage(),
        "No fields annotated with @PromptAnswer in class com.facebook.tools.io.TestIO$NotAnnotated"
      );
    }
  }

  @Test(groups = "fast")
  public void testUppercaseAnswer() {
    MockIO io = new MockIO();

    try {
      io.ask(UppercaseAnswer.FOO, "Did the test pass");
      Assert.fail("Expected exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(
        e.getMessage(),
        "Values must be all lower-case, but got Test for " +
          "com.facebook.tools.io.TestIO$UppercaseAnswer.FOO"
      );
    }
  }

  private static enum NotAnnotated {
    FOO,
  }

  private static enum UppercaseAnswer {
    @Answer("Test")
    FOO,
  }
}
