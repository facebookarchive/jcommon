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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Specifies valid command usage. See {@link com.facebook.tools.parser.CliCommand.Builder} for more
 * details.
 */
public class CliCommand {
  private final String name;
  private final List<String> description;
  private final List<String> notes;
  private final List<CliOption> options;
  private final List<CliParameter> parameters;
  private final boolean allowsTrailingParameters;

  private CliCommand(
    String name,
    List<String> description,
    List<String> notes,
    List<CliOption> options,
    List<CliParameter> parameters,
    boolean allowsTrailingParameters
  ) {
    this.name = name;
    this.description = new ArrayList<>(description);
    this.notes = new ArrayList<>(notes);
    this.options = new ArrayList<>(options);
    this.parameters = new ArrayList<>(parameters);
    this.allowsTrailingParameters = allowsTrailingParameters;
  }

  public String getName() {
    return name;
  }

  public List<String> getDescription() {
    return description;
  }

  public List<String> getNotes() {
    return notes;
  }

  public List<CliOption> getOptions() {
    return options;
  }

  public List<CliParameter> getParameters() {
    return parameters;
  }

  public boolean allowsTrailingParameter() {
    return allowsTrailingParameters;
  }

  public String getDocumentation() {
    StringBuilder result = new StringBuilder(80);

    appendDocumentation(result);

    return result.toString();
  }

  @Override
  public String toString() {
    return "CliCommandDefinition{" +
      "name='" + name + '\'' +
      '}';
  }

  public static class Builder {
    private final String name;
    private final List<String> description;
    private final List<CliOption.Builder> options = new ArrayList<>();
    private final List<CliParameter.Builder> parameters = new ArrayList<>();

    private List<String> notes = Collections.emptyList();
    private boolean allowsTrailingParameters;

    /**
     * Defines the command name and general description.
     *
     * @param name            the name of the command
     * @param description     the description displayed when printing usage help
     * @param additionalLines syntactic sugar for multi-line descriptions
     */
    public Builder(String name, String description, String... additionalLines) {
      this.name = name;
      this.description = new ArrayList<>();
      this.description.add(description);
      this.description.addAll(Arrays.asList(additionalLines));
    }

    /**
     * Adds additional documentation displayed after the main description and argument docs.
     *
     * @param notes
     * @param additionalLines
     * @return this builder
     */
    public Builder withNotes(String notes, String... additionalLines) {
      this.notes = new ArrayList<>();
      this.notes.add(notes);
      this.notes.addAll(Arrays.asList(additionalLines));

      return this;
    }

    /**
     * Adds a named option that takes a parameter, e.g., {@code --input foo.txt}.
     *
     * @param switchName           the name, including any dashes, e.g., {@code --input}
     * @param additionaSwitchNames synonyms for the name, e.g., {@code -i}
     * @return this builder
     */
    public CliOption.SwitchBuilder addOption(String switchName, String... additionaSwitchNames) {
      CliOption.SwitchBuilder builder = new CliOption.SwitchBuilder();

      builder.withSwitch(switchName);
      builder.withSwitch(additionaSwitchNames);
      options.add(builder);

      return builder;
    }

    /**
     * Adds a named option that doesn't take a parameter, e.g., {@code --debug}.
     *
     * @param switchName           the name, including any dashes, e.g., {@code --debug}
     * @param additionaSwitchNames synonyms for the name, e.g., {@code -d}
     * @return this builder
     */
    public CliOption.FlagBuilder addFlag(String switchName, String... additionaSwitchNames) {
      CliOption.FlagBuilder builder = new CliOption.FlagBuilder();

      builder.withSwitch(switchName);
      builder.withSwitch(additionaSwitchNames);
      options.add(builder);

      return builder;
    }

    /**
     * Adds a positional parameter. For example:
     * <code>
     * CliCommand.Builder builder = new CliCommand.Builder("cat", "Prints the contents of a file");
     * builder.addParameter("file").withDescription("The file to print")
     * </code>
     *
     * @param name the name used to refer to the parameter as this position
     * @return this builder
     */
    public CliParameter.Builder addParameter(String name) {
      CliParameter.Builder builder = CliParameter.Builder.withName(name);

      parameters.add(builder);

      return builder;
    }

    public Builder allowTrailingParameters() {
      this.allowsTrailingParameters = true;

      return this;
    }

    public CliCommand build() {
      List<CliOption> options = new ArrayList<>();
      Set<String> names = new HashSet<>(this.options.size());

      for (CliOption.Builder builder : this.options) {
        CliOption option = builder.build();

        for (String switchName : option.getSwitchNames()) {
          if (!names.add(switchName)) {
            throw new IllegalStateException("Switch name collision: " + switchName);
          }
        }

        options.add(option);
      }

      List<CliParameter> parameters = new ArrayList<>(this.parameters.size());

      for (CliParameter.Builder builder : this.parameters) {
        CliParameter parameter = builder.build();

        if (!names.add(parameter.getName())) {
          throw new IllegalStateException("Parameter name collision: " + parameter.getName());
        }

        parameters.add(parameter);
      }

      return new CliCommand(
        name, description, notes, options, parameters, allowsTrailingParameters
      );
    }
  }

  private void appendDocumentation(StringBuilder result) {
    result.append(getName());

    for (CliParameter parameter : getParameters()) {
      result.append(" <").append(parameter.getName()).append(">");
    }

    for (String line : getDescription()) {
      result.append('\n').append("  ").append(line);
    }

    if (!getOptions().isEmpty()) {
      result.append('\n');

      for (CliOption option : getOptions()) {
        result.append('\n');
        appendDocumentation(result, option);
      }
    }

    if (!getNotes().isEmpty()) {
      result.append('\n');

      for (String note : getNotes()) {
        result.append('\n').append("  ").append(note);
      }
    }
  }

  private void appendDocumentation(StringBuilder result, CliOption option) {
    Iterator<String> switchNames = option.getSwitchNames().iterator();

    result.append("  ");

    while (switchNames.hasNext()) {
      result.append(switchNames.next());

      if (switchNames.hasNext()) {
        result.append(' ');
      }
    }

    if (!option.isFlag()) {
      result.append(" <").append(option.getMetavar()).append('>');
    }

    result.append('\n');
    result.append("    [").append(option.isRequired() ? "Required" : "Optional").append(']');

    Iterator<String> descriptionIterator = option.getDescription().iterator();

    if (descriptionIterator.hasNext()) {
      result.append(" ");

      while (descriptionIterator.hasNext()) {
        result.append(descriptionIterator.next());

        if (descriptionIterator.hasNext()) {
          result.append("\n").append("               ");
        }
      }
    }

    for (String example : option.getExamples()) {
      result.append("\n").append("    e.g., ").append(example);
    }

    if (option.getDefaultValue() != null && !option.isFlag()) {
      result.append("\n").append("    default: ").append(option.getDefaultValue());
    }
  }
}
