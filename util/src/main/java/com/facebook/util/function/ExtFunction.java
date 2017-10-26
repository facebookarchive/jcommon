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

import com.facebook.util.ExtSupplier;
import java.util.Objects;
import java.util.function.Function;

public interface ExtFunction<T, R, E extends Throwable> {
  R apply(T t) throws E;
  
  default <V> ExtFunction<V, R, E> compose(ExtFunction<? super V, ? extends T, E> before) {
    Objects.requireNonNull(before);
    return (v) -> apply(before.apply(v));
  }
  
  default <V> ExtFunction<T, V, E> andThen(ExtFunction<? super R, ? extends V, E> after) {
    Objects.requireNonNull(after);
    return (t) -> after.apply(apply(t));
  }
  
  static <T, R> Function<T, R> quiet(ExtFunction<T, R, ?> function) {
    return (t) -> ExtSupplier.quiet(() -> function.apply(t)).get();
  }
}
