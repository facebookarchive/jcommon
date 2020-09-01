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
package com.facebook.util.digest;

/** adapter for the "repeatable murmur3 hash", MurmurHash createRepeatableHasher() */
public class LongMurmur3Hash implements DigestFunction<Long> {
  private static final LongMurmur3Hash INSTANCE = new LongMurmur3Hash();

  private final MurmurHash hasher = MurmurHash.createRepeatableHasher();

  /**
   * optionally use a singleton instance as the hasher is stateless, and hence thread safe
   *
   * @return
   */
  public static LongMurmur3Hash getInstance() {
    return INSTANCE;
  }

  @Override
  public long computeDigest(Long input) {
    return hasher.hash(input);
  }
}
