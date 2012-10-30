package com.facebook.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class StandardObjectMapper {
  public static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
    MAPPER.disable(MapperFeature.AUTO_DETECT_GETTERS);
    MAPPER.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    // to allow C/C++ style comments in JSON (non-standard, disabled by default)
    JsonFactory jsonFactory = MAPPER.getJsonFactory();
    jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
    // to allow (non-standard) unquoted field names in JSON:
    jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    // to allow use of apostrophes (single quotes), non standard
    jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
    jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    jsonFactory.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
    MAPPER.registerModule(new GuavaModule());
  }

  private StandardObjectMapper() {
    throw new AssertionError();
  }
}

