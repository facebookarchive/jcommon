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
import java.util.function.DoubleUnaryOperator;

public interface ExtDoubleUnaryOperator<E extends Throwable> {
  double applyAsDouble(double operand) throws E;
  
  default ExtDoubleUnaryOperator<E> compose(ExtDoubleUnaryOperator<E> before) {
    Objects.requireNonNull(before);
    return (operand) -> applyAsDouble(before.applyAsDouble(operand));
  }
  
  default ExtDoubleUnaryOperator<E> andThen(ExtDoubleUnaryOperator<E> after) {
    Objects.requireNonNull(after);
    return (operand) -> after.applyAsDouble(applyAsDouble(operand));
  }
  
  static DoubleUnaryOperator quiet(ExtDoubleUnaryOperator<?> doubleUnaryOperator) {
    return (operand) -> ExtDoubleSupplier.quiet(() -> doubleUnaryOperator.applyAsDouble(operand)).getAsDouble();
  }
}
