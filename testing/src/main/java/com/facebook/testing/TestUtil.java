package com.facebook.testing;


import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUtil {
  public static <T> Function<T> noOpFunction(){
    return new Function<T>() {
      @Override
      public void execute(T argument) {
        // noting
      }
    };
  }

  public static AtomicInteger countCompletedRunnables(
    int numTasks, Function<Runnable> submissionCallback
  ) {
    final AtomicInteger completed = new AtomicInteger(0);
    
    for (int i = 0; i < numTasks; i++) {
      submissionCallback.execute(new Runnable() {
        @Override
        public void run() {
          completed.incrementAndGet();
        }
      });
    }
    
    return completed;
  }

  public static <V> AtomicInteger countCompletedCallables(
    int numTasks, Function<Callable<V>> submissionCallback
  ) {
    final AtomicInteger completed = new AtomicInteger(0);
    
    for (int i = 0; i < numTasks; i++) {
      submissionCallback.execute(new Callable<V>() {
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
   * a bit hackish, we wait until the thread is not runnable or new to indicate
   * that is is blocked on a lock or monitor
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
}

