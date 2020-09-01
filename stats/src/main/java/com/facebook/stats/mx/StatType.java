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
package com.facebook.stats.mx;

/**
 * String wrapper that can memoize and efficiently do prepend/append/INSERT
 *
 * <p>For use with counters that need a default and a dynamic one (ex: per- category)
 */
public interface StatType {
  /** @return the current key */
  public String getKey();

  /**
   * @param suffix - append ".suffix"
   * @return returns getKey() + suffix
   */
  public StatType append(String suffix);

  /**
   * @param prefix - prepend "prefix."
   * @return returns prefix + getKey()
   */
  public StatType prepend(String prefix);

  /**
   * will create StatType such that
   *
   * <p>getKey() = prefix + value + suffix
   *
   * <p>but leaves the internal prefix + suffix the same. ex:
   *
   * <p>materialize("x").materialize("y").getKey() = prefix + "y" + suffix
   *
   * @param value - string to use as replacement value
   * @return new StatType on which getKey() returns as specified
   */
  public StatType materialize(String value);
}
