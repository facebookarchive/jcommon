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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class OneOfConverter implements CliConverter<String> {
  private final Set<String> possibilities;

  public OneOfConverter(Collection<String> possibilities) {
    this.possibilities = new LinkedHashSet<>(possibilities);
  }

  public static OneOfConverter oneOf(Collection<String> posibilities) {
    return new OneOfConverter(posibilities);
  }

  public static OneOfConverter oneOf(String... posibilities) {
    return new OneOfConverter(Arrays.asList(posibilities));
  }

  @Override
  public String convert(String value) throws Exception {
    if (!possibilities.contains(value)) {
      throw new ErrorMessage("Expected one of %s, but got %s", possibilities, value);
    }

    return value;
  }
}
