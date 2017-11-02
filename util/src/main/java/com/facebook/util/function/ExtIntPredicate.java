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
import java.util.function.IntPredicate;

public interface ExtIntPredicate<E extends Throwable> {
  boolean test(int value) throws E;
  
  default ExtIntPredicate<E> and(ExtIntPredicate<E> other) {
    Objects.requireNonNull(other);
    return (value) -> test(value) && other.test(value);
  }
  
  default ExtIntPredicate<E> negate() {
    return (value) -> !test(value);
  }
  
  default ExtIntPredicate<E> or(ExtIntPredicate<E> other) {
    Objects.requireNonNull(other);
    return (value) -> test(value) || other.test(value);
  }
  
  static IntPredicate quiet(ExtIntPredicate<?> intPredicate) {
    return (value) -> ExtBooleanSupplier.quiet(() -> intPredicate.test(value)).getAsBoolean();
  }
}
