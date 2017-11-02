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
package com.facebook.util.function;

import java.util.function.ToIntBiFunction;

public interface ExtToIntBiFunction<T, U, E extends Throwable> {
  int applyAsInt(T t, U u) throws E;
  
  static <T, U> ToIntBiFunction<T, U> quiet(ExtToIntBiFunction<T, U, ?> toIntBiFunction) {
    return (t, u) -> ExtIntSupplier.quiet(() -> toIntBiFunction.applyAsInt(t, u)).getAsInt();
  }
}
