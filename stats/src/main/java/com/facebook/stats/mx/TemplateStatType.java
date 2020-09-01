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
 * efficient implementation where you want a default key and a base key
 *
 * <p>example:
 *
 * <p>We want getKey() to return "scribe.all_cat.network_store.sent.bytes" but want to be able to
 * efficient create per-category value: "scribe.per_cat.category.network_store.sent.bytes"
 *
 * <p>We don't want to prepend "scribe.all_cat" every time we need this default so we memoize a
 * default key, but store the prefix/suffix for the per-category key.
 */
public class TemplateStatType implements StatType {
  private final String basePrefix;
  private final String baseSuffix;
  private String defaultKey;

  public TemplateStatType(String basePrefix, String baseSuffix, String defaultKey) {
    this.basePrefix = basePrefix;
    this.baseSuffix = baseSuffix;
    this.defaultKey = defaultKey;
  }

  @Override
  public String getKey() {
    internalMaterialize();

    return defaultKey;
  }

  // materializes the default key; handles null values for
  // basePrefix and baseSuffix
  private void internalMaterialize() {
    if (defaultKey == null) {
      if (basePrefix == null && baseSuffix == null) {
        defaultKey = null;
      } else if (basePrefix == null) {
        defaultKey = baseSuffix;
      } else if (baseSuffix == null) {
        defaultKey = basePrefix;
      } else {
        defaultKey = basePrefix + baseSuffix;
      }
    }
  }

  @Override
  public StatType append(String suffix) {
    return new TemplateStatType(
        basePrefix, baseSuffix == null ? suffix : baseSuffix + suffix, null);
  }

  @Override
  public StatType prepend(String prefix) {
    return new TemplateStatType(
        basePrefix == null ? prefix : prefix + basePrefix, baseSuffix, null);
  }

  @Override
  public StatType materialize(String value) {
    // optimize to remove a string concatenation
    if (value != null && !value.isEmpty()) {
      return new TemplateStatType(basePrefix, baseSuffix, basePrefix + value + baseSuffix);
    } else {
      return new TemplateStatType(basePrefix, baseSuffix, basePrefix + baseSuffix);
    }
  }
}
