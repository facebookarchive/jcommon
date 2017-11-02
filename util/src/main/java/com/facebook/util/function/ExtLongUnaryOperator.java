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
import java.util.function.LongUnaryOperator;

public interface ExtLongUnaryOperator<E extends Throwable> {
  long applyAsLong(long operand) throws E;
  
  default ExtLongUnaryOperator<E> compose(ExtLongUnaryOperator<E> before) {
    Objects.requireNonNull(before);
    return (operand) -> applyAsLong(before.applyAsLong(operand));
  }
  
  default ExtLongUnaryOperator<E> andThen(ExtLongUnaryOperator<E> after) {
    Objects.requireNonNull(after);
    return (operand) -> after.applyAsLong(applyAsLong(operand));
  }
  
  static LongUnaryOperator quiet(ExtLongUnaryOperator<?> longUnaryOperator) {
    return (operand) -> ExtLongSupplier.quiet(() -> longUnaryOperator.applyAsLong(operand)).getAsLong();
  }
}
