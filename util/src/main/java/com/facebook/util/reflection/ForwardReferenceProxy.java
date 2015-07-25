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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class ForwardReferenceProxy<T> {
  private final AtomicReference<T> instanceRef;
  private final Supplier<T> proxySupplier;

  public ForwardReferenceProxy(final Class<T> clazz) {
    this.instanceRef = new AtomicReference<>();
    proxySupplier = Suppliers.memoize(
      new Supplier<T>() {
        @Override
        public T get() {
          return wrap(clazz, instanceRef);
        }
      }
    );
  }

  public ForwardReferenceProxy<T> setInstance(T instance) {
    instanceRef.set(instance);

    return this;
  }

  public T get() {
    return proxySupplier.get();
  }

  private static <T> T wrap(Class<T> clazz, final AtomicReference<T> instance) {
    Preconditions.checkNotNull(instance, "must pass a non-null atomic reference");
    InvocationHandler handler = (proxy, method, args) -> method.invoke(
      Preconditions.checkNotNull(
        instance.get(),
        "instance has not been set"
      ), args
    );

    T wrapper = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);

    return wrapper;
  }
}
