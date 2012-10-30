package com.facebook.util;

import org.apache.commons.lang.StringEscapeUtils;

public class StringUtils {
  private StringUtils() {
    throw new AssertionError();
  }

  public static String stripQuotes(String input) {
    if (input.startsWith("'") || input.startsWith("\"")) {
      return StringEscapeUtils.unescapeJava(input.substring(1, input.length() - 1));
    } else {
      return input;
    }
  }
}
