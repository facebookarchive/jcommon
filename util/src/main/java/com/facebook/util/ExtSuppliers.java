package com.facebook.util;

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
//  NOTE: ADAPTED FROM: com.google.common.base.Supplier
public class ExtSuppliers {
  public static <T, E extends Throwable> ExtSupplier<T, E> memoize(ExtSupplier<T, E> supplier) {
    return new MemoizingExtSupplier<>(supplier);
  }

  public static <T, E extends Throwable> RefreshableExtSupplier<T, E> memoizeAllowRefresh(ExtSupplier<T, E> supplier) {
    return new MemoizingExtSupplier<>(supplier);
  }

  public static <T, E extends Throwable> ExtSupplier<T, E> ofInstance(T instance) {
    return new InstanceExtSupplier<>(instance);
  }

  private static class MemoizingExtSupplier<T, E extends Throwable> implements RefreshableExtSupplier<T, E> {
    private final ExtSupplier<T, E> delegate;
    private volatile boolean shouldCalculateValue = true;
    private T value;

    private MemoizingExtSupplier(ExtSupplier<T, E> delegate) {
      this.delegate = delegate;
    }

    @SuppressWarnings({"DoubleCheckedLocking", "SynchronizeOnThis"})
    @Override
    public T get() throws E {
      if (shouldCalculateValue) {
        synchronized (this) {
          if (shouldCalculateValue) {
            T t = delegate.get();

            value = t;
            shouldCalculateValue = false;

            return t;
          }
        }
      }

      return value;
    }

    @Override
    public void reset() {
      shouldCalculateValue = true;
    }
  }

  private static class InstanceExtSupplier<T, E extends Throwable> implements ExtSupplier<T, E> {
    private final T instance;

    private InstanceExtSupplier(T instance) {this.instance = instance;}

    @Override
    public T get() throws E {
      return instance;
    }
  }
}
