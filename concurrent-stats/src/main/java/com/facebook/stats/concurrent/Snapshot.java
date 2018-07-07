/*
 * Copyright (C) 2018 Facebook, Inc.
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
package com.facebook.stats.concurrent;

import java.util.Objects;

public class Snapshot {
  private final String type;
  private final long allTime;
  private final long hour;
  private final long tenMinute;
  private final long minute;

  public Snapshot(String type, long allTime, long hour, long tenMinute, long minute) {
    this.type = type;
    this.allTime = allTime;
    this.hour = hour;
    this.tenMinute = tenMinute;
    this.minute = minute;
  }

  public String getType() {
    return type;
  }

  public long getAllTime() {
    return allTime;
  }

  public long getHour() {
    return hour;
  }

  public long getTenMinute() {
    return tenMinute;
  }

  public long getMinute() {
    return minute;
  }

  Snapshot rate(long elapsed) {
    long total = getAllTime();

    if (elapsed == 0) {
      return new Snapshot("rate", total, total, total, total);
    }

    return new Snapshot(
      "rate",
      total / elapsed,
      elapsed < 3600 ? total / elapsed : getHour() / 3600,
      elapsed < 600 ? total / elapsed : getTenMinute() / 600,
      elapsed < 60 ? total / elapsed : getMinute() / 60
    );
  }

  @Override
  public String toString() {
    return "Snapshot{" +
      "type='" + type + '\'' +
      ", allTime=" + allTime +
      ", hour=" + hour +
      ", tenMinute=" + tenMinute +
      ", minute=" + minute +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Snapshot snapshot = (Snapshot) o;
    return allTime == snapshot.allTime &&
      hour == snapshot.hour &&
      tenMinute == snapshot.tenMinute &&
      minute == snapshot.minute &&
      Objects.equals(type, snapshot.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, allTime, hour, tenMinute, minute);
  }
}
