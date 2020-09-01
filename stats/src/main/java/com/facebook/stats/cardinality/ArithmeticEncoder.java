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
package com.facebook.stats.cardinality;

import com.facebook.stats.cardinality.Model.SymbolInfo;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;

class ArithmeticEncoder {
  private final Model model;
  private final OutputStream out;

  private long low;
  private long high = 0xFFFFFFFFFFFFL;

  private int underflowHighValue;
  private int underflowBytes;

  public ArithmeticEncoder(Model model, OutputStream out) {
    Preconditions.checkNotNull(model, "model is null");
    Preconditions.checkNotNull(out, "out is null");

    this.model = model;
    this.out = out;
  }

  public void encode(int symbol) throws IOException {
    // lookup symbol data
    SymbolInfo symbolInfo = model.getSymbolInfo(symbol);

    // adjust low and high counts
    long range = (high - low + 1) >> model.log2MaxCount();
    high = low + (range * symbolInfo.highCount()) - 1;
    low = low + range * symbolInfo.lowCount();

    // write high byte if they are equal
    while ((high & 0xFF0000000000L) == (low & 0xFF0000000000L)) {
      int value = (int) (high >>> 40);
      out.write(value);

      // write underflow bytes
      int underflowValue = (value == underflowHighValue) ? 0x00 : 0xFF;
      while (underflowBytes > 0) {
        out.write(underflowValue);
        underflowBytes--;
      }

      // remove high byte
      low <<= 8;
      high = (high << 8) | 0xFF;
    }
    low &= 0xFFFFFFFFFFFFL;
    high &= 0xFFFFFFFFFFFFL;

    // handle possible underflow
    // if top two bytes differ by only one digit
    if ((high >> 32) - (low >> 32) == 1) {
      // if second highest bytes are 0x00 on the high and 0xFF
      // on the low, we need to deal with underflow
      while ((high & 0x00FF00000000L) == 0 && (low & 0x00FF00000000L) == 0x00FF00000000L) {
        // if this is the first underflow byte remember the high value
        // so when we output later we know if we need to output 0xFF or 0x00
        if (underflowBytes == 0) {
          underflowHighValue = (int) (high >>> 40);
        }

        underflowBytes++;

        // remove second chunk of low and high (shifting over lower bits)
        low = removeUnderflowByte(low, 0x00);
        high = removeUnderflowByte(high, 0xFF);
      }
    }
  }

  public void close() throws IOException {
    // Write out the shortest value between the high and low values

    // if there are no underflow bytes...
    if (underflowBytes == 0) {
      // the high byte will be separated by more then one, so the
      // high byte plus one will be between the high and low values
      out.write((int) (low >>> 40) + 1);
    }
    // we have underflow, but if the second byte is 0xFF...
    else if ((low & 0x00FF00000000L) == 0x00FF00000000L) {
      // This is a complex case, that almost never happens
      //
      // In this case the high bytes are separated by only one, and
      // the subsequent underflow bytes on the high are 0x00 and low 0xFF.
      // The the final byte on the low is 0xFF and the high will be
      // anything other than 0x00 (since this would have been considered
      // an underflow byte).  So in decimal we have something like this:
      //   low: 3 99999 9
      //  high: 4 00000 1
      //
      // so if we simply out put the high byte of the high value, it will
      // be between the low and high.  In the example above, that would be
      // the equivalent of:
      //  value: 4
      //
      out.write((int) (high >>> 40));
    } else {
      // Slightly simpler case
      //
      // As above high bytes are separated by one, and underflow bytes
      // are 0xFF and 0x00 for the low and high respectively.  The final
      // byte on the low is anything but 0xFF and the high can be anything.
      // In decimal we have something like this:
      //    low: 3 99999 7
      //   high: 4 00000 0
      //
      // So we will need to output the high byte of the low value, the
      // underflow bytes (0xFF), and finally the second byte of the low
      // plus one, which will put the value between the low and the high.
      // In the example above, that would be the equivalent of:
      //  value: 3 99999 8

      // write the high byte of the low value
      out.write((int) (low >>> 40));

      // write the underflow bytes for the low (0xFF)
      while (underflowBytes > 0) {
        out.write(0xFF);
        underflowBytes--;
      }

      // write the second byte of the low value plus one to put it
      // between the low and high
      int secondByte = (int) ((low >>> 32) & 0xFF);
      out.write(secondByte + 1);
    }
  }

  public static long removeUnderflowByte(long value, int backFillValue) {
    long highBits = (value & 0xFF0000000000L);
    long lowBits = (value & 0x0000FFFFFFFFL) << 8;
    long newValue = highBits | lowBits | backFillValue;
    return newValue;
  }
}
