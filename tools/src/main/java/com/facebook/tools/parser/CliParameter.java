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
import java.util.List;

public class CliParameter {
  private final String name;
  private final List<String> description;
  private final List<String> examples;
  private final String defaultValue;
  private final boolean required;

  private CliParameter(
    String name,
    List<String> description,
    List<String> examples,
    String defaultValue,
    boolean required
  ) {
    this.name = name;
    this.description = new ArrayList<>(description);
    this.examples = new ArrayList<>(examples);
    this.defaultValue = defaultValue;
    this.required = required;
  }

  public String getName() {
    return name;
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

  public static class Builder {
    private final String name;

    private List<String> description = Collections.emptyList();
    private List<String> examples = Collections.emptyList();
    private String defaultValue;
    private boolean required = true;

    private Builder(String name) {
      this.name = name;
    }

    public static Builder withName(String name) {
      return new Builder(name);
    }

    public Builder withDescription(String description, String... additionalLines) {
      this.description = new ArrayList<>();
      this.description.add(description);
      this.description.addAll(Arrays.asList(additionalLines));

      return this;
    }

    public Builder withExample(String example, String... additionalLines) {
      this.examples = new ArrayList<>();
      this.examples.add(example);
      this.examples.addAll(Arrays.asList(additionalLines));

      return this;
    }

    public Builder withDefault(String defaultValue) {
      this.defaultValue = defaultValue;
      this.required = false;

      return this;
    }

    CliParameter build() {
      return new CliParameter(name, description, examples, defaultValue, required);
    }
  }
}
