package com.facebook.util.digest;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Murmur Hash 3 from http://sites.google.com/site/murmurhash/
 */
@ThreadSafe
public class MurmurHash {
  //  one constant seed we use when, for the same input at two arbitrary executions, spanning JVM
  // instances, hosts, etc, the hash is meant to produce the same digest for "all timeF"
  public static final long JCOMMON_SEED = 1318007700;

  private final long seed;
  private final HashFunction byteArrayHasher = Hashing.murmur3_128((int) JCOMMON_SEED);

  public MurmurHash(long seed) {
    this.seed = seed;
  }

  /**
   * This returns our MurmurHasher such that hash(x) = y "for all time"
   * @return
   */
  public static MurmurHash createRepeatableHasher() {
    return new MurmurHash(JCOMMON_SEED);
  }

  private long rotateLeft64(long x, int r) {
    return (x << r) | (x >>> (64 - r));
  }

  private long fmix(long k) {
    k ^= k >>> 33;
    k *= 0xff51afd7ed558ccdL;
    k ^= k >>> 33;
    k *= 0xc4ceb9fe1a85ec53L;
    k ^= k >>> 33;

    return k;
  }

  /**
   * Hash the given byte array into 128-bit values
   *
   * use guava imple
   *
   * @param data data to hash
   * @return 128 bits of hash result
   */
  public byte[] hash(byte[] data) {
    HashCode hashCode = byteArrayHasher.hashBytes(data);

    return hashCode.asBytes();
  }

  public long hashToLong(byte[] data) {
    HashCode hashCode = byteArrayHasher.hashBytes(data);

    return hashCode.asLong();
  }


  /**
   * A special version for long integers
   *
   * @param data the data to hash
   * @return lower 64 bites of the 128-bit hash result.
   */
  public long hash(long data) {
    long c1 = 0x87c37b91114253d5L;
    long c2 = 0x4cf5ad432745937fL;

    long h1 = seed, h2 = seed;

    long k1 = data;
    k1 *= c1;
    k1 = rotateLeft64(k1, 31);
    k1 *= c2;
    h1 ^= k1;

    h1 ^= 8;
    h2 ^= 8;

    h1 += h2;
    h2 += h1;

    return (fmix(h1) + fmix(h2));
  }
}
