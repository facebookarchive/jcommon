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
