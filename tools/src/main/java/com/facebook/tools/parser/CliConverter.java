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

import java.util.List;

public interface CliConverter<T> {
  public static CliConverter<String> STRING = new CliConverter<String>() {
    @Override
    public String convert(String value) throws Exception {
      return value;
    }
  };
  public static CliConverter<Integer> INT = new CliConverter<Integer>() {
    @Override
    public Integer convert(String value) throws Exception {
      return value == null ? null : Integer.parseInt(value);
    }
  };
  public static CliConverter<List<Integer>> INT_LIST = new RangeListConverter();
  public static CliConverter<List<String>> LIST = new ListConverter<>(STRING);
  public static CliConverter<Boolean> BOOLEAN = new CliConverter<Boolean>() {
    @Override
    public Boolean convert(String value) throws Exception {
      return value == null ? null : Boolean.valueOf(value);
    }
  };

  public T convert(String value) throws Exception;
}
