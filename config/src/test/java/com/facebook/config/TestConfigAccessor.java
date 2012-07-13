package com.facebook.config;

import org.joda.time.Period;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TestConfigAccessor {
  private JSONObject jsonObject1;
  private ConfigAccessor configAccessor;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    jsonObject1 = new JSONObject();
    
    JSONObject jsonBean1 = new JSONObject();
    
    jsonBean1.put("x", "600");
    jsonBean1.put("y", "6000000000");
    jsonBean1.put("class", "java.lang.Object");

    jsonObject1.put("fuu", jsonBean1);
    jsonObject1.put("list", Arrays.asList("1d", "7d"));
    jsonObject1.put("addr", 1601);
    configAccessor = new ConfigAccessor(jsonObject1);

  }
  
  @Test(groups = "fast")
  public void testGetList() throws Exception {
    List<Period> periodList = 
      configAccessor.getList("list", new StringToPeriodMapper());
    
    Assert.assertEquals(periodList.size(), 2);
    // StringToPeriod mapper actually converts to millis, so this will 
    // map to 24h, not "1 day", etc
    Assert.assertEquals(periodList.get(0), Period.hours(24));
    Assert.assertEquals(periodList.get(1), Period.hours(24 * 7));
  }

  @Test(groups = "fast")
  public void testExtractBean() throws Exception {
    Fuu bean = configAccessor.getBean("fuu", Fuu.FuuBuilder.class);

    Assert.assertEquals(bean.getX(), 600);
    Assert.assertEquals(bean.getY(), 6000000000L);
    Assert.assertEquals(bean.getSomeClass(), Object.class);
    Assert.assertEquals(bean.getS(), "baar");
  }

  @Test(groups = "fast")
  public void testNumberAsString() throws Exception {
    // tests that if a json value can be a string or number our accessors allow this
  	Assert.assertEquals(configAccessor.getString("addr"), "1601");
  	Assert.assertEquals(configAccessor.getInt("addr"), 1601);
  }

  @Test(groups = "fast", expectedExceptions = ConfigException.class)
  public void testMissingKey() throws Exception {
    // JSONObject throws on missing keys, we translate to ConfigException
  	configAccessor.getString("this_key_is_not_here");
  }

  private static class Fuu {
    private final int x;
    private final long y;
    private final Class<?> someClass;
    private final String s;

    private Fuu(int x, long y, Class<?> someClass, String s) {
      this.x = x;
      this.y = y;
      this.someClass = someClass;
      this.s = s;
    }

    public int getX() {
      return x;
    }

    public long getY() {
      return y;
    }

    public Class<?> getSomeClass() {
      return someClass;
    }

    public String getS() {
      return s;
    }

    public static class FuuBuilder implements ExtractableBeanBuilder<Fuu> {
      private int x;
      private long y;
      private Class<?> someClass;
      private String s = "baar";

      @FieldExtractor(key = "x", extractorClass = IntegerExtractor.class)
      public void setX(int x) {
        this.x = x;
      }

      @FieldExtractor(key = "y", extractorClass = LongExtractor.class)
      public void setY(long y) {
        this.y = y;
      }

      @FieldExtractor(key = "class", extractorClass = ClassExtractor.class)
      public void setSomeClass(Class<?> someClass) {
        this.someClass = someClass;
      }

      @FieldExtractor(
        key = "s", extractorClass = StringExtractor.class, optional = true
      )
      public void setS(String s) {
        this.s = s;
      }

      @Override
      public Fuu build() {
        return new Fuu(x, y, someClass, s);
      }
    }
  }
}