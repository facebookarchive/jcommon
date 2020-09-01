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

import com.google.common.primitives.Longs;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** old class using MD5 to do hasing. Better to use */
@Deprecated
public class LongDigestFunction implements DigestFunction<Long> {
  private final ThreadLocal<MessageDigest> digest =
      new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
          try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            return messageDigest;
          } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
          }
        }
      };

  @Override
  public long computeDigest(Long input) {
    byte[] bytes = Longs.toByteArray(input);

    return new BigInteger(digest.get().digest(bytes)).longValue();
  }
}
