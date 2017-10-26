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
import java.util.function.Predicate;

public interface ExtPredicate<T, E extends Throwable> {
  boolean test(T t) throws E;
  
  default ExtPredicate<T, E> and(ExtPredicate<? super T, E> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }
  
  default ExtPredicate<T, E> negate() {
    return (t) -> !test(t);
  }
  
  default ExtPredicate<T, E> or(ExtPredicate<? super T, E> other) {
    Objects.requireNonNull(other);
    return (t) -> test(t) || other.test(t);
  }
  
  static <T> Predicate<T> quiet(ExtPredicate<T, ?> predicate) {
    return (t) -> ExtBooleanSupplier.quiet(() -> predicate.test(t)).getAsBoolean();
  }
}
