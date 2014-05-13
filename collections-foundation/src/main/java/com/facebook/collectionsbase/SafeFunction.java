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

/**
 * the point of this is to restore the utility and equivalence of guava's "Function"
 * in the case that we have code that users our "Function".  The only exception you can throw there
 * or here is a runtime.  The parent, you can declare a checked exception which is useful.
 *
 * @param <K>
 * @param <V>
 */
public interface SafeFunction<K, V> extends Function<K, V, RuntimeException> {
  public V execute(K input);
}
