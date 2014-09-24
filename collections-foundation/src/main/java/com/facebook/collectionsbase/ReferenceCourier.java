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
package com.facebook.collectionsbase;

import javax.annotation.Nullable;

public class ReferenceCourier<T> implements Courier<T> {
  private final T instance;

  public ReferenceCourier(@Nullable T instance) {
    this.instance = instance;
  }

  public static <T2> ReferenceCourier<T2> empty() {
    return new ReferenceCourier<>(null);
  }

  @Override
  public boolean isSet() {
    return instance != null;
  }

  @Override
  public T get() {
    if (instance == null) {
      throw new NullPointerException();
    }

    return instance;
  }
}
