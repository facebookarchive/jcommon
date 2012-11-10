package com.facebook.data.types;

import com.facebook.util.serialization.SerDeException;
import com.facebook.util.serialization.SerDeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

public class TestDatumSerDe {

  private DatumSerDe serDe;
  private BooleanDatum trueDatum;
  private BooleanDatum falseDatum;
  private LongDatum longDatum;
  private ShortDatum shortDatum;
  private IntegerDatum integerDatum;
  private FloatDatum floatDatum;
  private DoubleDatum doubleDatum;
  private StringDatum stringDatum;
  private ByteDatum byteDatum;
  private ListDatum listDatum;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    serDe = new DatumSerDe();
    trueDatum = new BooleanDatum(true);
    falseDatum = new BooleanDatum(false);
    byteDatum = new ByteDatum((byte) 99);
    shortDatum = new ShortDatum((short) 100);
    integerDatum = new IntegerDatum(100000);
    longDatum = new LongDatum(300000000L);
    floatDatum = new FloatDatum(32.7f);
    doubleDatum = new DoubleDatum(10.12312415211431);
    stringDatum = new StringDatum("Fuu");
    listDatum = new ListDatum(
      Arrays.asList(
        integerDatum,
        longDatum,
        stringDatum
      )
    );
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    testValue(trueDatum);
    testValue(falseDatum);
    testValue(byteDatum);
    testValue(shortDatum);
    testValue(integerDatum);
    testValue(longDatum);
    testValue(floatDatum);
    testValue(doubleDatum);
    testValue(stringDatum);
    testValue(listDatum);
  }

  private void testValue(Datum datum) throws SerDeException {
    Datum processedValue = SerDeUtils.deserializeFromBytes(
      SerDeUtils.serializeToBytes(datum, serDe), serDe
    );
    Assert.assertEquals(processedValue, datum);
  }
}
