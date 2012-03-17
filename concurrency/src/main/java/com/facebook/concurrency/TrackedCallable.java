package com.facebook.concurrency;

import java.util.concurrent.Callable;

public interface TrackedCallable<V> extends Callable<V>, Completable {
}
