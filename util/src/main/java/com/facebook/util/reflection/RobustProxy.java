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
package com.facebook.util.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RobustProxy {
  public static <T> T wrap(Class<T> clazz, final T instance) {
    InvocationHandler handler = new InvocationHandler() { @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (instance != null) {
          return method.invoke(instance, args);
        } else if (method.getReturnType().equals(Void.class)) {
          return null;
        } else { // instance == null and caller expects a value
//          return wrap(method.getReturnType(), null);
          throw new UnsupportedOperationException();
        }
      }
    };
//    Class proxyClass = Proxy.getProxyClass( clazz.getClassLoader(), new Class[]{clazz});
//    T wrapper = (T) proxyClass.getConstructor(new Class[]{InvocationHandler.class}).newInstance(new Object[]{handler});

    T wrapper = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);

    return wrapper;
  }
}
