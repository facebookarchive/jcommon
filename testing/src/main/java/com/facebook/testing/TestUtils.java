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
package com.facebook.testing;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;

public class TestUtils {
  private TestUtils() {
    throw new AssertionError();
  }

  public static <T> Function<T> noOpFunction() {
    return new Function<T>() {
      @Override
      public void execute(T argument) {
        // noting
      }
    };
  }

  public static AtomicInteger countCompletedRunnables(
      int numTasks, Function<Runnable> submissionCallback) {
    final AtomicInteger completed = new AtomicInteger(0);

    for (int i = 0; i < numTasks; i++) {
      submissionCallback.execute(
          new Runnable() {
            @Override
            public void run() {
              completed.incrementAndGet();
            }
          });
    }

    return completed;
  }

  public static <V> AtomicInteger countCompletedCallables(
      int numTasks, Function<Callable<V>> submissionCallback) {
    final AtomicInteger completed = new AtomicInteger(0);

    for (int i = 0; i < numTasks; i++) {
      submissionCallback.execute(
          new Callable<V>() {
            @Override
            public V call() throws Exception {
              completed.incrementAndGet();

              return null;
            }
          });
    }

    return completed;
  }

  public static String generateString(int start, int length) {
    try {
      return new String(generateSequentialBytes(start, length), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] generateSequentialBytes(int start, int length) {
    byte[] result = new byte[length];

    for (int i = 0; i < length; i++) {
      result[i] = (byte) ((start + i) % 127);
    }

    return result;
  }

  /**
   * a bit hackish, we wait until the thread is not runnable or new to indicate that is is blocked
   * on a lock or monitor
   *
   * @param t thread to wait for
   */
  public static void waitUntilThreadBlocks(Thread t) {
    Thread.State state = t.getState();

    while (true) {
      switch (state) {
        case NEW:
        case RUNNABLE:
          state = t.getState();
          continue;
        case TERMINATED:
        case BLOCKED:
        case TIMED_WAITING:
        case WAITING:
          return;
      }
    }
  }

  public static Thread runInThread(Runnable runnable) {
    return runInThread(runnable, null);
  }

  public static Thread runInThread(Runnable runnable, String threadName) {
    Thread t = new Thread(runnable);

    if (threadName != null) {
      t.setName(threadName);
    }

    t.start();

    return t;
  }

  /**
   * generate count distinct moments in time, 1 second apart, then randomly order them
   *
   * @param baseDateTime start moment
   * @param count how many moments to generate
   * @return
   */
  public static List<DateTime> generateMoments(DateTime baseDateTime, int count) {
    List<DateTime> result = new ArrayList<DateTime>(count);

    for (int i = 0; i < count; i++) {
      result.add(baseDateTime.plusSeconds(i));
    }
    Collections.shuffle(result);

    return result;
  }

  /**
   * Returns a set of timezones that are distinct form each other in the kind of offsets they have
   * from UTC. Note: This method returns timezones having the same offsets but are different in that
   * they have fixed or non-fixed offsets. However, it's possible that two timezones with non-fixed
   * offsets have different transitions, only one of such timezones will be returned here.
   */
  public static Collection<DateTimeZone> getDistinctTimeZones() {
    Multimap<Integer, DateTimeZone> timeZones = LinkedHashMultimap.create();
    // figure out a set of timezones with unique offsets
    for (Object obj : DateTimeZone.getAvailableIDs()) {
      String id = (String) obj;
      DateTimeZone dateTimeZone = DateTimeZone.forID(id);
      TimeZone timeZone = dateTimeZone.toTimeZone();
      timeZones.put(timeZone.getRawOffset(), dateTimeZone);
      timeZones.put(timeZone.getRawOffset() + timeZone.getDSTSavings(), dateTimeZone);
    }
    Set<DateTimeZone> distinctZones = new HashSet<DateTimeZone>();
    for (Map.Entry<Integer, Collection<DateTimeZone>> entry : timeZones.asMap().entrySet()) {
      DateTimeZone fixedZone = null;
      DateTimeZone unfixedZone = null;
      for (DateTimeZone timeZone : entry.getValue()) {
        if (timeZone.isFixed()) {
          if (fixedZone == null) {
            fixedZone = timeZone;
          }
        } else {
          if (unfixedZone == null) {
            unfixedZone = timeZone;
          }
        }
      }
      // Collect one instance each of fixed and non-fixed timezones
      if (fixedZone != null) {
        distinctZones.add(fixedZone);
      }
      if (unfixedZone != null) {
        distinctZones.add(unfixedZone);
      }
    }
    return distinctZones;
  }

  public static void assertContains(String haystack, Object needle) {
    Assert.assertTrue(
        haystack != null && haystack.contains(String.valueOf(needle)),
        String.format("Expected to find '%s' in: %s", needle, haystack));
  }

  /**
   * Same as TestNG's {@link Assert#assertEquals(Object, Object)} except the message text is
   * guaranteed to use the {@link Object#toString()} of the objects. Useful for short maps and lists
   * where seeing all the elements is more useful than just the one differing element or size
   * difference.
   *
   * @param actual
   * @param expected
   */
  public static void assertEqualsWithNiceMessage(Object actual, Object expected) {
    assertEqualsWithNiceMessage(actual, expected, null);
  }

  /**
   * Same as TestNG's {@link Assert#assertEquals(Object, Object)} except the message text is
   * guaranteed to use the {@link Object#toString()} of the objects. Useful for short maps and lists
   * where seeing all the elements is more useful than just the one differing element or size
   * difference.
   *
   * @param actual actual value
   * @param expected expected value
   * @param message custom message to prepend to default message
   */
  public static void assertEqualsWithNiceMessage(Object actual, Object expected, String message) {
    message = message == null ? "" : (message + " ");

    // using the same formatting as Assert.format() makes Idea do nice UI things (e.g., "Click to
    // see difference")
    assertEquals(
        actual, expected, message + "expected:<" + expected + "> but was:<" + actual + ">");
  }
}
