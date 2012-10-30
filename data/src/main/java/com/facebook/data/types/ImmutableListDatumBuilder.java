package com.facebook.data.types;


import com.google.common.collect.ImmutableList;

public class ImmutableListDatumBuilder {
  private final ImmutableList.Builder<Datum> builder =
    new ImmutableList.Builder<>();
  
  public ImmutableListDatumBuilder add(ListDatum datum) {
    builder.add(datum);

    return this;
    
  }
  
  public ImmutableListDatumBuilder add(long value) {
    builder.add(new LongDatum(value));
    
    return this;
    
  }
  
  public ImmutableListDatumBuilder add(int value) {
    builder.add(new IntegerDatum(value));
    
    return this;

  }
  public ImmutableListDatumBuilder add(String value) {
    builder.add(new StringDatum(value));
    
    return this;
  }
  
  public ListDatum build() {
    return new ListDatum(builder.build());
  }
}
