package com.facebook.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * annotation for use with set<FIELD>() methods to indicate how to get a value
 * from JSON to populate the field via the set method
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldExtractor {
  public String key();
  public Class<?> extractorClass();
  public boolean optional() default false;
}
