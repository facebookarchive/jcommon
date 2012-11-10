package com.facebook.data.types;

import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeUtils;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMapDatum {

  private MapDatum primes1;
  private MapDatum primes2;
  private MapDatum fibonacci;
  private MapDatum factorials;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    primes1 = new MapDatum(
      new ImmutableMap.Builder<Datum, Datum>()
        .put(new LongDatum(1), new LongDatum(2))
        .put(new LongDatum(2), new LongDatum(3))
        .put(new LongDatum(3), new LongDatum(5))
        .put(new LongDatum(4), new LongDatum(7))
        .build()
    );
    primes2 = new MapDatum(
      new ImmutableMap.Builder<Datum, Datum>()
        .put(new LongDatum(1), new LongDatum(2))
        .put(new LongDatum(2), new LongDatum(3))
        .put(new LongDatum(3), new LongDatum(5))
        .put(new LongDatum(4), new LongDatum(7))
        .build()
    );
    fibonacci = new MapDatum(
      new ImmutableMap.Builder<Datum, Datum>()
        .put(new LongDatum(0), new LongDatum(0))
        .put(new LongDatum(1), new LongDatum(1))
        .put(new LongDatum(2), new LongDatum(1))
        .put(new LongDatum(3), new LongDatum(2))
        .put(new LongDatum(4), new LongDatum(3))
        .build()
    );
    factorials = new MapDatum(
      new ImmutableMap.Builder<Datum, Datum>()
        .put(new LongDatum(0), new LongDatum(1))
        .put(new LongDatum(1), new LongDatum(1))
        .put(new LongDatum(2), new LongDatum(2))
        .put(new LongDatum(3), new LongDatum(6))
        .put(new LongDatum(4), new LongDatum(24))
        .build()
    );
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    // check equality
    Assert.assertEquals(primes1.compareTo(primes1), 0);
    Assert.assertEquals(primes1.compareTo(primes2), 0);
    Assert.assertEquals(primes1.compareTo(factorials), 1);
    Assert.assertEquals(primes1.compareTo(factorials), 1);
    Assert.assertEquals(factorials.compareTo(primes1), -1);
    Assert.assertEquals(fibonacci.compareTo(factorials), -1);
    Assert.assertEquals(factorials.compareTo(fibonacci), 1);
  }

  @Test(groups = "fast")
  public void testEquality() throws Exception {
    Assert.assertEquals(primes1, primes1);
    Assert.assertEquals(primes1, primes2);
    Assert.assertFalse(factorials.equals(primes2));
  }

  @Test(groups = "fast")
  public void testAsString() throws Exception {
    JSONObject primesJsonObject = new JSONObject(primes1.asString());

    Assert.assertEquals(primesJsonObject.length(), 4);
    Assert.assertEquals(primesJsonObject.getString("1"), "2");
    Assert.assertEquals(primesJsonObject.getString("2"), "3");
    Assert.assertEquals(primesJsonObject.getString("3"), "5");
    Assert.assertEquals(primesJsonObject.getString("4"), "7");
  }
  
  @Test(groups = "fast")
  public void testAsJsonString() throws Exception {
   Assert.assertEquals(
     DatumUtils.buildJSON(primes1).toString(), "{\"3\":5,\"2\":3,\"1\":2,\"4\":7}"
   );
  }
  
  @Test(groups = "fast")
  public void testAsBytes() throws Exception {
    // asBytes() == asString, so if above task passes, and these are equal, we're good
    String bytesAsString = new String(primes1.asBytes());

    Assert.assertEquals(bytesAsString, primes1.asString());
  }
  
  @Test(groups = "fast")
  public void testSerDe() throws Exception {
    SerDe<Datum> serDe = new MapDatum.SerDeImpl();
    Assert.assertEquals(
      primes1,
      SerDeUtils.deserializeFromBytes(
        SerDeUtils.serializeToBytes(primes1, serDe),
        serDe
      )
    );
  }
}
