package com.facebook.stats;

public interface ReadableMultiWindowRate {
  public long getMinuteSum();  
  public long getMinuteRate();  
  public long getTenMinuteSum();  
  public long getTenMinuteRate();  
  public long getHourSum();
  public long getHourRate();
  public long getAllTimeSum(); 
  public long getAllTimeRate(); 
}
