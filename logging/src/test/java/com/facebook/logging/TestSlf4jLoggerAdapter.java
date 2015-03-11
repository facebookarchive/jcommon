/*
 * Copyright (C) 2015 Facebook, Inc.
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
package com.facebook.logging;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestSlf4jLoggerAdapter {

  @Test
  public void testCallForwarding() throws Exception {
    for (Method method : Slf4jLoggerAdapter.class.getDeclaredMethods()) {
        Logger mockLogger = Mockito.mock(Logger.class);
        org.slf4j.Logger adapter = new Slf4jLoggerAdapter(mockLogger);
        Object[] nullParams = new Object[method.getParameterTypes().length];
        method.invoke(adapter, nullParams);
        verifyCallForwarded(mockLogger, method);
    }
  }

  private void verifyCallForwarded(Logger mockLogger, Method method) {
    switch (method.getName()) {
      case "error":
        if (hasThrowableParam(method)) {
          verify(mockLogger, times(1)).error(any(Throwable.class), anyString(), anyVararg());
        } else {
          verify(mockLogger, times(1)).error(anyString(), Matchers.<Object[]>anyVararg());
        }
        break;
      case "warn":
        if (hasThrowableParam(method)) {
          verify(mockLogger, times(1)).warn(any(Throwable.class), anyString(), anyVararg());
        } else {
          verify(mockLogger, times(1)).warn(anyString(), Matchers.<Object[]>anyVararg());
        }
        break;
      case "info":
        if (hasThrowableParam(method)) {
          verify(mockLogger, times(1)).info(any(Throwable.class), anyString(), anyVararg());
        } else {
          verify(mockLogger, times(1)).info(anyString(), Matchers.<Object[]>anyVararg());
        }
        break;
      case "debug":
        if (hasThrowableParam(method)) {
          verify(mockLogger, times(1)).debug(any(Throwable.class), anyString(), anyVararg());
        } else {
          verify(mockLogger, times(1)).debug(anyString(), Matchers.<Object[]>anyVararg());
        }
        break;
      case "trace":
        if (hasThrowableParam(method)) {
          verify(mockLogger, times(1)).trace(any(Throwable.class), anyString(), anyVararg());
        } else {
          verify(mockLogger, times(1)).trace(anyString(), anyVararg());
        }
        break;
      case "getName":
        verify(mockLogger, times(1)).getName();
        break;
      case "isErrorEnabled":
        verify(mockLogger, times(1)).isErrorEnabled();
        break;
      case "isDebugEnabled":
        verify(mockLogger, times(1)).isDebugEnabled();
        break;
      case "isTraceEnabled":
        verify(mockLogger, times(1)).isTraceEnabled();
        break;
      case "isInfoEnabled":
        verify(mockLogger, times(1)).isInfoEnabled();
        break;
      case "isWarnEnabled":
        verify(mockLogger, times(1)).isWarnEnabled();
        break;
      default:
        Assert.fail("Unexpected method " + method.getName());
    }
  }

  private boolean hasThrowableParam(Method method) {
    for (Class<?> type : method.getParameterTypes()) {
      if (type.equals(Throwable.class)) {
        return true;
      }
    }
    return false;
  }
}
