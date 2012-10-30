// QuickLZ data compression library
// Copyright (C) 2006-2008 Lasse Mikkel Reinhold
// lar@quicklz.com
//
// QuickLZ can be used for free under the GPL-1 or GPL-2 license
// (where anything released into public must be open source) or under
// a commercial license if such has been acquired (see
// http://www.quicklz.com/order.html). The commercial license does not
// cover derived or ported versions created by third parties under
// GPL.
//
// Java port of version 1.4.0. Only a subset of the C library has been
// ported, namely level 1 not in streaming mode.

package com.facebook.util.compress;

public class QuickLz {

  // The port is compatible with the C version with following settings:
  public final int QLZ_COMPRESSION_LEVEL = 1;
  public final int QLZ_STREAMING_BUFFER = 0;

  // No bounds checking code required because this is managed Java
  public final int QLZ_MEMORY_SAFE = 0;

  // QuickLZ Java version 1.4.0 final (negative revision means beta)
  public final int QLZ_VERSION_MAJOR = 1;
  public final int QLZ_VERSION_MINOR = 4;
  public final int QLZ_VERSION_REVISION = 0;

  final private static int HASH_VALUES = 4096;
  final private static int MINOFFSET = 2;
  final private static int UNCONDITIONAL_MATCHLEN = 6;
  final private static int UNCOMPRESSED_END = 4;
  final private static int CWORD_LEN = 4;
  final private static int DEFAULT_HEADERLEN = 9;

  static int headerLen(byte[] source)
  {
    return ((source[0] & 2) == 2) ? 9 : 3;
  }

  static public long sizeDecompressed(byte[] source)
  {
    if (headerLen(source) == 9)
      return fastread(source, 5, 4);
    else
      return fastread(source, 2, 1);
  }

  static public long sizeCompressed(byte[] source)
  {
    if (headerLen(source) == 9)
      return fastread(source, 1, 4);
    else
      return fastread(source, 1, 1);
  }

  public static byte[] compress(byte[] source)
  {
    int src = 0;
    int headerlen = DEFAULT_HEADERLEN;
    int dst = headerlen + CWORD_LEN;
    long cword_val = 0x80000000L;
    int cword_ptr = headerlen;
    byte[] destination = new byte[source.length + 400];
    int[] hashtable = new int[HASH_VALUES];
    int[] cachetable = new int[HASH_VALUES];
    byte[] hash_counter = new byte[HASH_VALUES];
    byte[] d2;
    int fetch = 0;
    int last_matchstart = (source.length - UNCONDITIONAL_MATCHLEN - 
        UNCOMPRESSED_END - 1);

    if(source.length == 0)
      return new byte[0];

    if(src <= last_matchstart)
      fetch = (int)fastread(source, src, 3);

    while (src <= last_matchstart)
    {
      if ((cword_val & 1) == 1)
      {
        if (src > 3 * (source.length >> 2) && dst > src - (src >> 5))
        {
          d2 = new byte[source.length + DEFAULT_HEADERLEN];
          d2[0] = 2 | 0;
          fastwrite(d2, 1, source.length + headerlen, 4);
          fastwrite(d2, 5, source.length, 4);
          System.arraycopy(source, 0, d2, headerlen, source.length);
          return d2;
        }

        fastwrite(destination, cword_ptr, (cword_val >>> 1) | 0x80000000L, 4);
        cword_ptr = dst;
        dst += CWORD_LEN;
        cword_val = 0x80000000L;
      }            

      int hash = ((fetch >>> 12) ^ fetch) & (HASH_VALUES - 1);
      int o = hashtable[hash];
      int cache = cachetable[hash] ^ fetch;

      cachetable[hash] = fetch;
      hashtable[hash] = src;

      if (cache == 0 && src - o > MINOFFSET && hash_counter[hash] != 0)
      {
        cword_val = ((cword_val >>> 1) | 0x80000000L);
        if (source[o + 3] != source[src + 3])
        {
          int f = 3 - 2 | (hash << 4);
          destination[dst + 0] = (byte)(f >>> 0 * 8);
          destination[dst + 1] = (byte)(f >>> 1 * 8);
          src += 3;
          dst += 2;
        }
        else
        {
          int old_src = src;
          int remaining = ((source.length - UNCOMPRESSED_END - src + 1 - 1)>255 
              ? 255 
                  : (source.length - UNCOMPRESSED_END - src + 1 - 1));

          src += 4;
          if (source[o + src - old_src] == source[src])
          {
            src++;
            if (source[o + src - old_src] == source[src])
            {
              src++;
              while (source[o + (src - old_src)] == source[src] && 
                  (src - old_src) < remaining)
                src++;
            }
          }

          int matchlen = src - old_src;

          hash <<= 4;
          if (matchlen < 18)
          {
            int f = hash | (matchlen - 2);
            // Neither Java nor C# wants to inline fastwriteN
            destination[dst + 0] = (byte)(f >>> 0 * 8);
            destination[dst + 1] = (byte)(f >>> 1 * 8);
            dst += 2;
          }
          else
          {
            int f = hash | (matchlen << 16);
            fastwrite(destination, dst, f, 3);
            dst += 3;
          }
        }
        fetch = (int)fastread(source, src, 3);
      }
      else
      {
        hash_counter[hash] = 1;
        destination[dst] = source[src];
        cword_val = (cword_val >>> 1);
        src++;
        dst++;
        fetch = ((fetch >>> 8) & 0xffff) | 
        ((((int)source[src + 2]) & 0xff) << 16);
      }
    }

    while (src <= source.length - 1)
    {
      if ((cword_val & 1) == 1)
      {
        fastwrite(destination, cword_ptr, 
            (long)((cword_val >>> 1) | 0x80000000L), 4);
        cword_ptr = dst;
        dst += CWORD_LEN;
        cword_val = 0x80000000L;
      }

      destination[dst] = source[src];
      src++;
      dst++;
      cword_val = (cword_val >>> 1);
    }
    while ((cword_val & 1) != 1)
    {
      cword_val = (cword_val >>> 1);
    }
    fastwrite(destination, cword_ptr, (long)((cword_val >>> 1) | 0x80000000L), 
        CWORD_LEN);
    destination[0] = 2 | 1;
    fastwrite(destination, 1, (long)dst, 4);
    fastwrite(destination, 5, (long)source.length, 4);
    d2 = new byte[dst];
    System.arraycopy(destination, 0, d2, 0, dst);      
    return d2;
  }

  static long fastread(byte[] a, int i, int numbytes)
  {
    long l = 0;
    switch (numbytes)
    {
    case 3:
      l |= ((((int)a[i + 0]) & 0xffL) << 0*8);
      l |= ((((int)a[i + 1]) & 0xffL) << 1*8);
      l |= ((((int)a[i + 2]) & 0xffL) << 2*8);
      break;

    case 2:
      l |= ((((int)a[i + 0]) & 0xffL) << 0*8);
      l |= ((((int)a[i + 1]) & 0xffL) << 1*8);
      break;
    case 1:
      l |= ((((int)a[i + 0]) & 0xffL) << 0*8);
      break;
    case 4:
      l |= ((((int)a[i + 0]) & 0xffL) << 0*8);
      l |= ((((int)a[i + 1]) & 0xffL) << 1*8);
      l |= ((((int)a[i + 2]) & 0xffL) << 2*8);
      l |= ((((int)a[i + 3]) & 0xffL) << 3*8);
      break;
    }
    return l;
  }

  static void fastwrite(byte[] a, int i, long value, int numbytes)
  {
    switch (numbytes)
    {
    case 3:
      a[i] = (byte)value;
      a[i + 1] = (byte)(value >>> 8);
      a[i + 2] = (byte)(value >>> 16);
      break;
    case 2:
      a[i] = (byte)value;
      a[i + 1] = (byte)(value >>> 8);
      break;
    case 4:
      a[i] = (byte)value;
      a[i + 1] = (byte)(value >>> 8);
      a[i + 2] = (byte)(value >>> 16);
      a[i + 3] = (byte)(value >>> 24);
      break;
    }
  }

  static public byte[] decompress(byte[] source)
  {
    int size = (int)sizeDecompressed(source);

    int src = headerLen(source);
    int dst = 0;
    long cword_val = 1;
    byte[] destination = new byte[size];
    int[] hashtable = new int[4096];
    byte[] hash_counter = new byte[4096];
    int last_matchstart = size - UNCONDITIONAL_MATCHLEN - UNCOMPRESSED_END - 1;
    int last_hashed = -1;
    int hash;
    int fetch = 0;

    if ((source[0] & 1) != 1)
    {
      byte[] d2 = new byte[size];
      System.arraycopy(source, headerLen(source), d2, 0, size);
      return d2;
    }

    for (; ; )
    {
      if (cword_val == 1)
      {
        cword_val = fastread(source, src, 4);
        src += 4;
        if (dst <= last_matchstart)
          fetch = (int)fastread(source, src, 3);
      }

      if ((cword_val & 1) == 1)
      {
        int matchlen;
        int offset2;

        cword_val = cword_val >>> 1;
      hash = (fetch >>> 4) & 0xfff;
      offset2 = hashtable[hash];

      if ((fetch & 0xf) != 0)
      {
        matchlen = (fetch & 0xf) + 2;
        src += 2;
      }
      else
      {
        matchlen = ((int)source[src + 2]) & 0xff;
        src += 3;
      }

      destination[dst + 0] = destination[offset2 + 0];
      destination[dst + 1] = destination[offset2 + 1];
      destination[dst + 2] = destination[offset2 + 2];

      for (int i = 3; i < matchlen; i += 1)
      {
        destination[dst + i] = destination[offset2 + i];
      }
      dst += matchlen;

      fetch = (int)fastread(destination, last_hashed + 1, 3); 
      // destination[last_hashed + 1] | (destination[last_hashed + 2] << 8) | 
      // (destination[last_hashed + 3] << 16);
      while (last_hashed < dst - matchlen)
      {
        last_hashed++;
        hash = ((fetch >>> 12) ^ fetch) & (HASH_VALUES - 1);
        hashtable[hash] = last_hashed;
        hash_counter[hash] = 1;
        fetch = fetch >>> 8 & 0xffff | 
        (((int)destination[last_hashed + 3]) & 0xff) << 16;
      }
      last_hashed = dst - 1;
      fetch = (int)fastread(source, src, 3);
      }
      else
      {
        if (dst <= last_matchstart)
        {
          destination[dst] = source[src];
          dst += 1;
          src += 1;
          cword_val = cword_val >>> 1;

        while (last_hashed < dst - 3)
        {
          last_hashed++;
          int fetch2 = (int)fastread(destination, last_hashed, 3);
          hash = ((fetch2 >>> 12) ^ fetch2) & (HASH_VALUES - 1);
          hashtable[hash] = last_hashed;
          hash_counter[hash] = 1;
        }
        fetch = fetch >> 8 & 0xffff | (((int)source[src + 2]) & 0xff) << 16;  
        }
        else
        {
          while (dst <= size - 1)
          {
            if (cword_val == 1)
            {
              src += CWORD_LEN;
              cword_val = 0x80000000L;
            }

            destination[dst] = source[src];
            dst++;
            src++;
            cword_val = cword_val >>> 1;
          }

          byte[] d2 = new byte[size];
          System.arraycopy(destination, 0, d2, 0, size);
          return d2;
        }
      }
    }
  }
}

