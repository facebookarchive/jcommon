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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class ArgumentList {
  private final int originalSize;
  private final List<Argument> arguments;

  ArgumentList(List<String> arguments) {
    this.originalSize = arguments.size();
    this.arguments = new ArrayList<>(arguments.size());

    for (String argument : arguments) {
      this.arguments.add(new Argument(this.arguments.size(), argument));
    }
  }

  public List<Map.Entry<String, String>> removeSwitchValues(String switchName) {
    return removeSwitchValues(switchName, false);
  }

  public List<Map.Entry<String, String>> removeFlag(String flagName) {
    return removeSwitchValues(flagName, true);
  }

  public Iterator<String> trailing() {
    int trailingIndex = arguments.size() - 1;
    int expectedIndex = originalSize - 1;

    if (arguments.isEmpty() || arguments.get(trailingIndex).index != expectedIndex) {
      // either no unconsumed arguments, or they are not trailing
      return Collections.emptyIterator();
    }

    // scan backwards for the longest contiguous block of trailing arguments
    while (trailingIndex > 0 && arguments.get(trailingIndex).index == expectedIndex) {
      --trailingIndex;
      --expectedIndex;
    }

    return new TrailingIterator(arguments.subList(trailingIndex, arguments.size()).iterator());
  }

  public Iterator<String> remaining() {
    return new TrailingIterator(arguments.iterator());
  }

  private List<Map.Entry<String, String>> removeSwitchValues(String switchName, boolean flag) {
    Iterator<Argument> iterator = arguments.iterator();
    List<Map.Entry<String, String>> values = new ArrayList<>();

    while (iterator.hasNext()) {
      String argument = iterator.next().value;
      String value = null;

      if (flag) {
        if (switchName.equals(argument)) {
          iterator.remove(); // remove flag
          value = "true";
        }
      } else {
        if (argument.startsWith(switchName + "=")) {
          value = argument.substring((switchName + "=").length());
          iterator.remove(); // remove switch=value
        }

        if (switchName.equals(argument) && iterator.hasNext()) {
          iterator.remove(); // remove switch
          value = iterator.next().value;
          iterator.remove(); // remove value
        }
      }

      if (value != null) {
        values.add(new AbstractMap.SimpleImmutableEntry<>(switchName, value));
      }
    }

    return values;
  }

  private static class Argument {
    private final int index;
    private final String value;

    private Argument(int index, String value) {
      this.index = index;
      this.value = value;
    }
  }

  private static class TrailingIterator implements Iterator<String> {
    private final Iterator<Argument> delegate;

    private TrailingIterator(Iterator<Argument> delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public String next() {
      return delegate.next().value;
    }

    @Override
    public void remove() {
      delegate.remove();
    }
  }
}
