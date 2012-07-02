/**
 * Adapted from http://www.colloquial.com/ArithmeticCoding
 * Copyright (c) 2002, Bob Carpenter. All Rights Reserved.
 *
 * Adaptation Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.stats.cardinality;

import com.facebook.stats.cardinality.Model.SymbolInfo;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class ArithmeticDecoder {
  private final Model model;

  private long low;
  private long high;
  private long value;
  private int bufferedBytes;

  private final InputStream in;

  public ArithmeticDecoder(Model model, InputStream in) throws IOException {

    Preconditions.checkNotNull(model, "model is null");
    Preconditions.checkNotNull(in, "in is null");

    this.model = model;
    this.in = in;

    // We initialize the decoder with 48 bits (6 bytes) of input.
    for (int i = 0; i < 6; ++i) {
      bufferByte();
      ++bufferedBytes;
    }
  }

  public ArithmeticDecoder(Model model, byte[] bytes) throws IOException {
    this(model, new ByteArrayInputStream(Preconditions.checkNotNull(bytes, "bytes is null")));
  }

  public int decode() throws IOException {
    // determine next symbol
    // calculate the % of the value within the range
    long range = (high - low + 1) >>> model.log2MaxCount();

    int currentSymbolCount = (int) ((value - low) / range);
    SymbolInfo symbolInfo = model.countToSymbol(currentSymbolCount);

    high = low + (range * symbolInfo.highCount()) - 1;
    low = low + range * symbolInfo.lowCount();

    // if high bytes are equal, remove high byte and add a new byte of input
    while ((high & 0xFF0000000000L) == (low & 0xFF0000000000L)) {
      bufferByte();
    }

    // handle possible underflow
    // if top two bytes differ by only one digit
    if ((high >> 32) - (low >> 32) == 1) {
      // if second highest bytes are 0x00 on the high and 0xFF
      // on the low, we need to deal with underflow
      while ((high & 0x00FF00000000L) == 0 && (low & 0x00FF00000000L) == 0x00FF00000000L) {
        // remove second chunk of low and high (shifting over lower bits)
        low = removeUnderflowByte(low);
        high = removeUnderflowByte(high);
        value = removeUnderflowByte(value);

        // add a new byte
        bufferByte();
      }
    }

    low &= 0xFFFFFFFFFFFFL;
    high &= 0xFFFFFFFFFFFFL;
    value &= 0xFFFFFFFFFFFFL;

    return symbolInfo.symbol();
  }

  private void bufferByte() throws IOException {
    // shift over the high and low
    low <<= 8;
    high = (high << 8) | 0xFF;

    // read a byte and add to the value
    int nextByte = in.read();
    if (nextByte < 0) {
      if (bufferedBytes == 0) {
        return;
      }
      value <<= 8;
      --bufferedBytes;
    } else {
      value = (value << 8);
      value |= nextByte;
    }
  }

  public static long removeUnderflowByte(long value) {
    long highBits = (value & 0xFF0000000000L) >>> 8;
    long lowBits = value & 0x0000FFFFFFFFL;
    long newValue = highBits | lowBits;
    return newValue;
  }
}
