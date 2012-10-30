package com.facebook.data.types;

import com.google.common.collect.ImmutableList;

public class JsonListDatum extends ListDatum {
  public JsonListDatum(Iterable<Datum> datumList) {
    super(ImmutableList.copyOf(datumList));
  }

  @Override
  public String asString() {
    return DatumUtils.buildJSON(this).toString();
  }
}
