package com.facebook.util.digest;

import com.google.common.primitives.Longs;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * old class using MD5 to do hasing.  Better to use
 */
@Deprecated
public class LongDigestFunction implements DigestFunction<Long> {
  private final ThreadLocal<MessageDigest> digest = 
    new ThreadLocal<MessageDigest>(){
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
