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
package com.facebook.tools.example;

import com.facebook.tools.parser.CliConverter;
import com.google.common.net.HostAndPort;

import java.io.File;
import java.util.List;

public class Converters {
  public static CliConverter<Integer> INT = CliConverter.INT;
  public static CliConverter<List<Integer>> INT_LIST = CliConverter.INT_LIST;
  public static CliConverter<List<String>> LIST = CliConverter.LIST;
  public static CliConverter<Boolean> BOOLEAN = CliConverter.BOOLEAN;
  public static CliConverter<File> FILE = new CliConverter<File>() {
    @Override
    public File convert(String value) throws Exception {
      return value == null ? null : new File(value);
    }
  };
  public static CliConverter<HostAndPort> HOST_PORT = new CliConverter<HostAndPort>() {
    @Override
    public HostAndPort convert(String value) throws Exception {
      return value == null ? null : HostAndPort.fromString(value);
    }
  };
}
