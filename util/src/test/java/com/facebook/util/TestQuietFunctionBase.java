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

import com.facebook.util.exceptions.UncheckedCheckedException;
import java.io.IOException;
import java.util.function.Supplier;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class TestQuietFunctionBase {
  @Test(groups = "fast")
  public void testError() {
    testExceptionType(Error.class, Error.class);
  }
  
  @Test(groups = "fast")
  public void testRuntimeException() {
    testExceptionType(RuntimeException.class, RuntimeException.class);
  }
  
  @Test(groups = "fast")
  public void testCheckedException() {
    testExceptionType(IOException.class, UncheckedCheckedException.class);
  }
  
  protected abstract void throwFromQuiet(Supplier<? extends Throwable> toThrow);
  
  private void testExceptionType(Class<? extends Throwable> toThrow, Class<? extends Throwable> expectedCaughtType) {
    try {
      throwFromQuiet(() -> quietNewInstance(toThrow));
    } catch (Throwable caught) {
      Assert.assertEquals(caught.getClass(), expectedCaughtType);
    }
  }
  
  private <T> T quietNewInstance(Class<T> classToCreate) {
    try {
      return classToCreate.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
