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

public class RangeListConverter implements CliConverter<List<Integer>> {
  @Override
  public List<Integer> convert(String value) throws Exception {
    if (value == null) {
      return null;
    }

    List<String> ranges = CliConverter.LIST.convert(value);
    List<Integer> result = new ArrayList<>();

    for (String range : ranges) {
      int delimiterIndex = range.indexOf('-');

      if (delimiterIndex == -1) {
        result.add(INT.convert(range));
      } else {
        String begin = range.substring(0, delimiterIndex);
        String end = range.substring(delimiterIndex + 1);

        for (int i = INT.convert(begin); i <= INT.convert(end); i++) {
          result.add(i);
        }
      }
    }

    return result;
  }
}
