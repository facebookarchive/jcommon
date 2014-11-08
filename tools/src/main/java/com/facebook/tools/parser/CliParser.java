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

import com.facebook.tools.ErrorMessage;

import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies the command-line arguments match the {@link com.facebook.tools.parser.CliCommand}, and
 * makes it easy to extract arguments by name.
 */
public class CliParser {
  private final CliCommand command;
  private final Set<String> switches = new LinkedHashSet<>();
  private final Map<String, String> values = new LinkedHashMap<>();
  private final Map<String, List<String>> multiValues = new LinkedHashMap<>();
  private final List<String> trailing = new ArrayList<>();
  private final List<CliOption> missing = new ArrayList<>();
  private final List<String> unexpected = new ArrayList<>();
  private final List<Map.Entry<String, String>> duplicates = new ArrayList<>();

  public CliParser(CliCommand command, List<String> arguments) {
    this.command = command;

    // getOption removes options as it parses them
    ArgumentList argumentList = new ArgumentList(arguments);

    for (CliOption option : command.getOptions()) {
      switches.addAll(option.getSwitchNames());

      List<Map.Entry<String, String>> parsedValues = getOption(option, argumentList);

      if (parsedValues.isEmpty()) {
        if (option.isRequired()) {
          missing.add(option);
        }
      } else if (parsedValues.size() > 1) {
        if (option.isUnique()) {
          duplicates.addAll(parsedValues);
        } else {
          for (String switchName : option.getSwitchNames()) {
            for (Map.Entry<String, String> parsedValue : parsedValues) {
              List<String> switchValues = multiValues.get(switchName);

              if (switchValues == null) {
                switchValues = new ArrayList<>();
                multiValues.put(switchName, switchValues);
              }

              switchValues.add(parsedValue.getValue());
            }
          }
        }
      } else {
        String value = parsedValues.get(0).getValue();

        for (String switchName : option.getSwitchNames()) {
          values.put(switchName, value);
        }
      }
    }

    Iterator<CliParameter> parameters = command.getParameters().iterator();
    Iterator<String> trailingArguments = argumentList.trailing();

    while (parameters.hasNext() && trailingArguments.hasNext()) {
      String name = parameters.next().getName();

      values.put(name, trailingArguments.next());
      trailingArguments.remove();
      switches.add(name);
    }

    while (parameters.hasNext()) {
      CliParameter parameter = parameters.next();

      switches.add(parameter.getName());

      if (parameter.isRequired()) {
        CliOption option = new CliOption.SwitchBuilder()
          .withSwitch(parameter.getName())
          .build();

        missing.add(option);
      }
    }

    if (command.allowsTrailingParameter()) {
      while (trailingArguments.hasNext()) {
        trailing.add(trailingArguments.next());
        trailingArguments.remove();
      }
    }

    Iterator<String> unexpected = argumentList.remaining();

    while (unexpected.hasNext()) {
      this.unexpected.add(unexpected.next());
    }
  }

  public void verify(PrintStream out) {
    List<String> errors = new ArrayList<>();

    if (!missing.isEmpty()) {
      StringBuilder missingMessage = new StringBuilder(80);
      List<String> missingSwitches = new ArrayList<>(missing.size());

      for (CliOption option : missing) {
        missingSwitches.add(last(option.getSwitchNames()));
      }

      missingMessage.append("Missing required option");

      if (missing.size() > 1) {
        missingMessage.append('s');
      }

      missingMessage.append(": ").append(join(", ", missingSwitches));
      errors.add(missingMessage.toString());
    }

    if (!unexpected.isEmpty()) {
      String unexpectedMessage = "Unexpected parameters: " + join(" ", unexpected);

      errors.add(unexpectedMessage);
    }

    if (!duplicates.isEmpty()) {
      StringBuilder duplicatesMessage = new StringBuilder(80);
      List<String> duplicateSwitches = new ArrayList<>(duplicates.size());

      for (Map.Entry<String, String> duplicate : duplicates) {
        duplicateSwitches.add(duplicate.getKey() + "=" + duplicate.getValue());
      }

      duplicatesMessage.append("Duplicate options: ").append(join(", ", duplicateSwitches));
      errors.add(duplicatesMessage.toString());
    }

    if (!errors.isEmpty()) {
      out.println(command.getDocumentation());
      out.println();
      out.flush();

      throw new ErrorMessage(join("\n", errors));
    }
  }

  public String get(String option) {
    return get(option, CliConverter.STRING);
  }

  public <T> T get(String option, CliConverter<T> converter) {
    if (!switches.contains(option)) {
      throw new IllegalStateException(
        String.format("Expected option name to be one of %s, but got %s", switches, option)
      );
    }

    String value = values.get(option);

    try {
      return converter.convert(value);
    } catch (Exception e) {
      throw new ErrorMessage(e, "Failed to parse %s %s", option, value);
    }
  }

  public List<String> getMulti(String option) {
    return getMulti(option, CliConverter.STRING);
  }

  public <T> List<T> getMulti(String option, CliConverter<T> converter) {
    List<String> values = multiValues.get(option);

    if (values == null || values.isEmpty()) {
      // value must have been specified less than twice
      T value = get(option, converter);

      return value == null ? Collections.<T>emptyList() : Collections.singletonList(value);
    }

    List<T> result = new ArrayList<>();

    for (String value : values) {
      try {
        result.add(converter.convert(value));
      } catch (Exception e) {
        throw new ErrorMessage(e, "Failed to parse %s %s", option, value);
      }
    }

    return result;
  }

  public List<String> getTrailing() {
    return trailing;
  }

  public <T> List<T> getTrailing(CliConverter<T> converter) {
    List<T> result = new ArrayList<>();

    for (String value : trailing) {
      try {
        result.add(converter.convert(value));
      } catch (Exception e) {
        throw new ErrorMessage(e, "Failed to parse %s", value);
      }
    }

    return result;
  }

  private static List<Map.Entry<String, String>> getOption(
    CliOption option, ArgumentList argumentList
  ) {
    List<Map.Entry<String, String>> result = new ArrayList<>();
    boolean flag = option.isFlag();

    for (String switchName : option.getSwitchNames()) {
      List<Map.Entry<String, String>> values;

      if (flag) {
        values = argumentList.removeFlag(switchName);
      } else {
        values = argumentList.removeSwitchValues(switchName);
      }

      result.addAll(values);
    }

    if (result.isEmpty()) {
      String defaultValue = option.getDefaultValue();

      if (defaultValue != null) {
        Map.Entry<String, String> defaultEntry =
          new AbstractMap.SimpleImmutableEntry<>(last(option.getSwitchNames()), defaultValue);

        result.add(defaultEntry);
      }
    }

    return result;
  }

  private static String join(String separator, Iterable<?> values) {
    return join(separator, values.iterator());
  }

  private static String join(String separator, Iterator<?> values) {
    StringBuilder result = new StringBuilder(80);

    while (values.hasNext()) {
      result.append(values.next());

      if (values.hasNext()) {
        result.append(separator);
      }
    }

    return result.toString();
  }

  private static <T> T last(Iterable<T> values) {
    return last(values.iterator());
  }

  private static <T> T last(Iterator<T> values) {
    T result = null;

    while (values.hasNext()) {
      result = values.next();
    }

    return result;
  }
}
