package com.facebook.concurrency;

import java.util.concurrent.ExecutorService;

public interface ExecutorServiceFactory<T extends ExecutorService> {
  T create();
}
