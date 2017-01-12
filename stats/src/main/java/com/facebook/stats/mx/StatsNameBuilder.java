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

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Optional;

/**
 * Decides whether to include MBean attributes in the Stats object, and, if so, what to call them.
 */
public interface StatsNameBuilder {
  /**
   * Gets the name that should be used in the Stats for an attribute.
   *
   * @param bean the ObjectName of the bean where the attribute was found
   * @param attribute the name of the top-level attribute
   * @param key if the bean's top-level attribute was {@link CompositeData},
   *             the key used to get from there to the value being named.
   *
   * @return the name that should be used, or {@link Optional#none()} if it
   *         should be elided
   */
  Optional<String> name(ObjectName bean, String attribute, String key);
}
