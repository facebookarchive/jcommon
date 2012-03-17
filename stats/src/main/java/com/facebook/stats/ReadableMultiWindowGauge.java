package com.facebook.stats;

public interface ReadableMultiWindowGauge extends ReadableMultiWindowRate {
  long getMinuteSamples();
  long getMinuteAvg();
  long getTenMinuteSamples();
  long getTenMinuteAvg();
  long getHourSamples();
  long getHourAvg();
  long getAllTimeSamples();
  long getAllTimeAvg();
}
