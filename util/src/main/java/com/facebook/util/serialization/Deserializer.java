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
package com.facebook.util.serialization;


import java.io.DataInput;

/**
 * interface that takes a stream of bytes wrapped in the DataInput interface (to make
 * reading primitives easier), and builds a T. Implementations are often nested static classes
 * of Class\<T\> in order to have access to specialized, private constructors
 *
 * @param <T>
 */
public interface Deserializer<T> {
  public T deserialize(DataInput in) throws SerDeException;
}
