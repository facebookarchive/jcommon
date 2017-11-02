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

import java.util.Objects;
import java.util.function.BiPredicate;

public interface ExtBiPredicate<T, U, E extends Throwable> {
  boolean test(T t, U u) throws E;
  
  default ExtBiPredicate<T, U, E> and(ExtBiPredicate<? super T, ? super U, E> other) {
    Objects.requireNonNull(other);
    return (t, u) -> test(t, u) && other.test(t, u);
  }
  
  default ExtBiPredicate<T, U, E> negate() {
    return (t, u) -> !test(t, u);
  }
  
  default ExtBiPredicate<T, U, E> or(ExtBiPredicate<? super T, ? super U, E> other) {
    Objects.requireNonNull(other);
    return (t, u) -> test(t, u) || other.test(t, u);
  }
  
  static <T, U> BiPredicate<T, U> quiet(ExtBiPredicate<T, U, ?> biPredicate) {
    return (t, u) -> ExtBooleanSupplier.quiet(() -> biPredicate.test(t, u)).getAsBoolean();
  }
}
