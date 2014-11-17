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
import java.util.List;

public class ListConverter<T> implements CliConverter<List<T>> {
  private final CliConverter<T> converter;

  public ListConverter(CliConverter<T> converter) {
    this.converter = converter;
  }

  @Override
  public List<T> convert(String value) throws Exception {
    if (value == null) {
      return null;
    }

    List<T> result = new ArrayList<>();

    for (String item : value.split(",")) {
      result.add(converter.convert(item));
    }

    return result;
  }
}
