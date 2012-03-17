package com.facebook.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtil {
  private static final Pattern NUMBER_AND_UNIT =
    Pattern.compile("(\\d+)([a-zA-Z]+)?");

  /**
   * default unit is seconds
   * <p/>
   * 10 = 10,000
   * 10ms = 10
   * 11s = 11,000
   * 3m = 180,000
   * 1h = 3,600,000
   * 1d = 86,400,000
   * *
   *
   * @param duration string to translate
   * @return returns duration in millis
   */
  public static long getDurationMillis(String duration) {
    Matcher matcher = NUMBER_AND_UNIT.matcher(duration);

    if (matcher.matches()) {
      long number = Long.valueOf(matcher.group(1));

      if (matcher.group(2) != null) {
        String unitStr = matcher.group(2).toLowerCase();
        char unit = unitStr.charAt(0);

        switch (unit) {
          case 's':
            return number * 1000;
          case 'm':
            // if it's an m, could be 'minutes' or 'millis'.  default minutes
            if (unitStr.length() >= 2 && unitStr.charAt(1) == 's') {
              return number;
            }

            return number * 60 * 1000;
          case 'h':
            return number * 60 * 60 * 1000;
          case 'd':
            return number * 60 * 60 * 24 * 1000;
          case 'y':
            // JodaTime should handle leap year issues will handle leap years
            return number * 365 * 60 * 60 * 24 * 1000; 
          default:
            throw new ConfigException("unknown time unit :" + unit);
        }
      } else {
        return number;
      }
    } else {
      throw new ConfigException("malformed duration string: " + duration);
    }
  }

  public static long getSizeBytes(String size) {
    Matcher matcher = NUMBER_AND_UNIT.matcher(size);

    if (matcher.matches()) {
      long number = Long.valueOf(matcher.group(1));

      if (matcher.group(2) != null) {
        char unit = matcher.group(2).toLowerCase().charAt(0);

        switch (unit) {
          case 'b':
            return number;
          case 'k':
            return number * 1024;
          case 'm':
            return number * 1024 * 1024;
          case 'g':
            return number * 1024 * 1024 * 1024;
          default:
            throw new ConfigException("unknown size unit :" + unit);
        }
      } else {
        return number;
      }
    } else {
      throw new ConfigException("malformed size string: " + size);
    }
  }
}
