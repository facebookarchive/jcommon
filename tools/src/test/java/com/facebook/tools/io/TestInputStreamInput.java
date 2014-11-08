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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestInputStreamInput {
  @Test(groups = "fast")
  public void testRead() throws Exception {
    ByteArrayInputStream inputStream = new ByteArrayInputStream("foo".getBytes());
    InputStreamInput input = new InputStreamInput(inputStream);

    Assert.assertEquals(input.read(), 'f');
    Assert.assertEquals(input.read(), 'o');
    Assert.assertEquals(input.read(), 'o');
    Assert.assertEquals(input.read(), -1);
  }

  @Test(groups = "fast")
  public void testReadLine() throws Exception {
    ByteArrayInputStream inputStream = new ByteArrayInputStream("Foo bar\nHello".getBytes());
    InputStreamInput input = new InputStreamInput(inputStream);

    Assert.assertEquals(input.readLine(), "Foo bar");
    Assert.assertEquals(input.readLine(), "Hello");
    Assert.assertNull(input.readLine());
  }

  @Test(groups = "fast")
  public void testIterator() throws Exception {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      "This is\na test\nof iteration\n".getBytes()
    );
    InputStreamInput input = new InputStreamInput(inputStream);
    List<String> lines = new ArrayList<>();

    for (String line : input) {
      lines.add(line);
    }

    Assert.assertEquals(lines, Arrays.asList("This is", "a test", "of iteration"));
  }
}
