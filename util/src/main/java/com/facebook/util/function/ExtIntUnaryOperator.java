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
import java.util.function.IntUnaryOperator;

public interface ExtIntUnaryOperator<E extends Throwable> {
  int applyAsInt(int operand) throws E;
  
  default ExtIntUnaryOperator<E> compose(ExtIntUnaryOperator<E> before) {
    Objects.requireNonNull(before);
    return (operand) -> applyAsInt(before.applyAsInt(operand));
  }
  
  default ExtIntUnaryOperator<E> andThen(ExtIntUnaryOperator<E> after) {
    Objects.requireNonNull(after);
    return (operand) -> after.applyAsInt(applyAsInt(operand));
  }
  
  static IntUnaryOperator quiet(ExtIntUnaryOperator<?> intUnaryOperator) {
    return (operand) -> ExtIntSupplier.quiet(() -> intUnaryOperator.applyAsInt(operand)).getAsInt();
  }
}
