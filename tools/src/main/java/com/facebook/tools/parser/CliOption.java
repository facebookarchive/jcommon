/*
 * Copyright (C) 2014 Facebook, Inc.
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
package com.facebook.tools.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CliOption {
  private final Set<String> switchNames;
  private final String metavar;
  private final List<String> description;
  private final List<String> examples;
  private final String defaultValue;
  private final boolean required;
  private final boolean unique;
  private final boolean flag;

  private CliOption(
    Set<String> switchNames,
    String metavar,
    List<String> description,
    List<String> examples,
    String defaultValue,
    boolean required,
    boolean unique,
    boolean flag
  ) {
    this.switchNames = new LinkedHashSet<>(switchNames);
    this.metavar = metavar;
    this.description = new ArrayList<>(description);
    this.examples = new ArrayList<>(examples);
    this.defaultValue = defaultValue;
    this.required = required;
    this.unique = unique;
    this.flag = flag;
  }

  public Set<String> getSwitchNames() {
    return switchNames;
  }

  public String getMetavar() {
    return metavar;
  }

  public List<String> getDescription() {
    return description;
  }

  public List<String> getExamples() {
    return examples;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isUnique() {
    return unique;
  }

  public boolean isFlag() {
    return flag;
  }

  abstract static class Builder {
    abstract CliOption build();
  }

  public static class SwitchBuilder extends Builder {
    private final Set<String> switchNames = new LinkedHashSet<>();

    private String metavar = "option";
    private List<String> description = Collections.emptyList();
    private List<String> examples = Collections.emptyList();
    private String defaultValue;
    private boolean required = true;
    private boolean unique = true;

    public SwitchBuilder() {
    }

    public SwitchBuilder withSwitch(String... switchNames) {
      this.switchNames.addAll(Arrays.asList(switchNames));

      return this;
    }

    public SwitchBuilder withMetavar(String metavar) {
      this.metavar = metavar;

      return this;
    }

    public SwitchBuilder withDescription(String description, String... additionalLines) {
      this.description = new ArrayList<>();
      this.description.add(description);
      this.description.addAll(Arrays.asList(additionalLines));

      return this;
    }

    public SwitchBuilder withExample(String example, String... additionalLines) {
      this.examples = new ArrayList<>();
      this.examples.add(example);
      this.examples.addAll(Arrays.asList(additionalLines));

      return this;
    }

    public SwitchBuilder withDefault(String defaultValue) {
      this.defaultValue = defaultValue;
      this.required = false;

      return this;
    }

    public SwitchBuilder allowMultiple() {
      this.unique = false;

      return this;
    }

    CliOption build() {
      return new CliOption(
        switchNames, metavar, description, examples, defaultValue, required, unique, false
      );
    }
  }

  public static class FlagBuilder extends Builder {
    private final Set<String> switchNames = new LinkedHashSet<>();

    private List<String> description = Collections.emptyList();

    public FlagBuilder() {
    }

    public FlagBuilder withSwitch(String... switchNames) {
      this.switchNames.addAll(Arrays.asList(switchNames));

      return this;
    }

    public FlagBuilder withDescription(String description, String... additionalLines) {
      this.description = new ArrayList<>();
      this.description.add(description);
      this.description.addAll(Arrays.asList(additionalLines));

      return this;
    }

    CliOption build() {
      return new CliOption(
        switchNames,
        null,
        description,
        Collections.<String>emptyList(),
        "false",
        false,
        true,
        true
      );
    }
  }
}
