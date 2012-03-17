package com.facebook.config;

import com.facebook.collections.Mapper;
import org.joda.time.Period;

public class StringToPeriodMapper implements Mapper<String, Period> {
  @Override
  public Period map(String input) {
    return new Period(ConfigUtil.getDurationMillis(input));
  }
}
