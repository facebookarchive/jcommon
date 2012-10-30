package com.facebook.config.dynamic;

import com.google.common.base.Function;

public class OptionTranslator<From, To> extends OptionImpl<To> {
  public OptionTranslator(final Option<From> option, final Function<From, To> translator) {
    setValue(translator.apply(option.getValue()));
    option.addWatcher(new OptionWatcher<From>() {
      @Override
      public void propertyUpdated(From value) throws Exception {
        setValue(translator.apply(option.getValue()));
      }
    });
  }
}
