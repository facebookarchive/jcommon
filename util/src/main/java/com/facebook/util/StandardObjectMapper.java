/*
 * Copyright (C) 2012 Facebook, Inc.
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
package com.facebook.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class StandardObjectMapper {
  public static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
    MAPPER.disable(MapperFeature.AUTO_DETECT_GETTERS);
    MAPPER.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    // to allow C/C++ style comments in JSON (non-standard, disabled by default)
    JsonFactory jsonFactory = MAPPER.getFactory();
    jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
    // to allow (non-standard) unquoted field names in JSON:
    jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    // to allow use of apostrophes (single quotes), non standard
    jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
    jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    jsonFactory.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
    MAPPER.registerModule(new GuavaModule());
    MAPPER.registerModule(new JodaModule());
  }

  private StandardObjectMapper() {
    throw new AssertionError();
  }
}

