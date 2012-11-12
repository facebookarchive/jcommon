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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = NumberCachingDatum.class, name = "numberCachingDatum")
})
public class WrappedDatum implements Datum {
  private final Datum delegate;
  private final Class<? extends Datum> aClass;

  public WrappedDatum(Datum delegate) {
    Preconditions.checkArgument(delegate != null, "WrappedDatum requires non-null delegate");
    this.delegate = delegate;
    //noinspection ConstantConditions
    aClass = delegate.getClass();
  }

  @Override
  public boolean asBoolean() {
    return delegate.asBoolean();
  }

  @Override
  public byte asByte() {
    return delegate.asByte();
  }

  @Override
  public short asShort() {
    return delegate.asShort();
  }

  @Override
  public int asInteger() {
    return delegate.asInteger();
  }

  @Override
  public long asLong() {
    return delegate.asLong();
  }

  @Override
  public float asFloat() {
    return delegate.asFloat();
  }

  @Override
  public double asDouble() {
    return delegate.asDouble();
  }

  @Override
  public String asString() {
    return delegate.asString();
  }

  @Override
  public byte[] asBytes() {
    return delegate.asBytes();
  }

  @Override
  public boolean isNull() {
    return delegate.isNull();
  }

  @Override
  public DatumType getType() {
    return delegate.getType();
  }

  @Override
  public Object asRaw() {
    return delegate.asRaw();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    // can either be another Wrapped
    if (!(o instanceof WrappedDatum)){
      return o != null && !(o.getClass().isAssignableFrom(aClass)) && delegate.equals(o);
    }

    WrappedDatum that = (WrappedDatum) o;

    return !(delegate != null ? !delegate.equals(that.delegate) : that.delegate != null);

  }

  @Override
  public int hashCode() {
    return delegate != null ? delegate.hashCode() : 0;
  }

  @Override
  public int compareTo(Datum o) {
    return delegate.compareTo(o);
  }
}
