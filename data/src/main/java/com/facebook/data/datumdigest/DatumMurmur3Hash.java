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
package com.facebook.data.datumdigest;


import com.facebook.data.types.Datum;
import com.facebook.data.types.DatumType;
import com.facebook.util.digest.DigestFunction;
import com.facebook.util.digest.MurmurHash;

/**
 * takes Datums and delegates to a hash function pair:
 *  f1: long -> long
 *  f2: byte[] -> long
 *
 *  f2 has an option: internally, it produces 128 bit hash, so you can ask to use the upper or
 *  lower 8 bytes.  The default is the lower 8 bytes.  For random hashing, in practice it does
 *  not matter.
 */
public class DatumMurmur3Hash implements DatumDigest, DigestFunction<Datum> {
  // Since this digestfuncton needs to be deterministic, use some 
  // fixed number to initialize it. This number cannot be changed in
  // future. Puma instances can go up and down, and the old data still
  // needs to be read.
  private final MurmurHash hasher = MurmurHash.createRepeatableHasher();
  // applies only to to the hash(byte[]) where we have 128 bytes
  private final boolean useLsb;

  private DatumMurmur3Hash(boolean useLsb) {
    this.useLsb = useLsb;
  }


  public DatumMurmur3Hash() {
    this(true);
  }

  /**
   * for the case that input is not long-compatible and directly produces a long as a hash value,
   * we get a 128 bit hash. This means use the lower 8 bytes as the long to return
   *
   * @return digest that will use lower 8 bytes
   */
  public static DatumMurmur3Hash useLsb() {
    return new DatumMurmur3Hash(true);
  }

  /**
   * for the case that input is not long-compatible and directly produces a long as a hash value,
   * we get a 128 bit hash. This means use the upper 8 bytes as the long to return
   *
   * @return digest that will use upper 8 bytes
   */
  public static DatumMurmur3Hash useMsb() {
    return new DatumMurmur3Hash(false);
  }

  @Override
  public long computeDigest(Datum input) {
    // optimization to use the faster hash on a long when the type is an integer (in the
    // mathematical sense, not java/C types)
    if (DatumType.isLongCompatible(input)) {
      return hasher.hash(input.asLong());
    } else {
      // this uses guava's hash
      byte[] bytes = hasher.hash(input.asBytes());

      long value = 0;
      int start;
      int end;

      if (useLsb) {
        start = 8;
        end = 0;
      } else {
        start = 16;
        end = 9;
      }
      for (int i = start; i >= end; i--) {
        value <<= 8;
        value ^= bytes[i] & 0xFF;
      }

      return value;
    }
  }
}
