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
package com.facebook.concurrency.linearization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLinearizer {
  private static final Logger LOG = LoggerFactory.getLogger(TestLinearizer.class);

  private Linearizer linearizer;
  private AtomicInteger nextTaskId;
  private List<SerialStartTask> taskList;
  private List<Integer> results;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    linearizer = new Linearizer();
    nextTaskId = new AtomicInteger(0);
    taskList = new ArrayList<>();
    results = new ArrayList<>();
  }

  @Test(groups = "fast")
  public void testSanity1() throws Exception {
    // equivalent partial ordering: 1,1,2
    nextConcurrentTask();
    nextConcurrentTask();
    nextLinearizationTask();
    executeTasks();
    verifyResults();
  }

  @Test(groups = "fast")
  public void testSanity2() throws Exception {
    // equivalent partial ordering: 1,1,2,3,4,5,5,6
    nextConcurrentTask();
    nextConcurrentTask();
    nextLinearizationTask();
    nextConcurrentTask();
    nextLinearizationTask();
    nextConcurrentTask();
    nextConcurrentTask();
    nextLinearizationTask();
    executeTasks();
    verifyResults();
  }

  private void executeTasks() throws InterruptedException {
    ExecutorService executor = Executors.newCachedThreadPool();

    Collections.shuffle(taskList);

    for (SerialStartTask task : taskList) {
      executor.execute(task);
      task.waitForStart();
    }

    executor.shutdown();

    while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
      LOG.info("waited 10 seconds for executor shutdown, will wait some more");
    }
  }

  private void verifyResults() {
    for (int i = 1; i < results.size(); i++) {
      Assert.assertTrue(results.get(i - 1) <= results.get(i));
    }
  }

  private void nextConcurrentTask() {
    ConcurrentPoint concurrentPoint = linearizer.createConcurrentPoint();
    int taskId = nextTaskId.get();
    SerialStartTask task =
        new SerialStartTask(
            new Runnable() {
              @Override
              public void run() {
                concurrentPoint.start();

                try {
                  results.add(taskId);
                } finally {
                  concurrentPoint.complete();
                }
              }
            });

    taskList.add(task);
  }

  private void nextLinearizationTask() {
    LinearizationPoint linearizationPoint = linearizer.createLinearizationPoint();
    int taskId = nextTaskId.incrementAndGet();

    nextTaskId.incrementAndGet();

    SerialStartTask task =
        new SerialStartTask(
            new Runnable() {
              @Override
              public void run() {
                linearizationPoint.start();

                try {
                  results.add(taskId);
                } finally {
                  linearizationPoint.complete();
                }
              }
            });

    taskList.add(task);
  }

  private static class SerialStartTask implements Runnable {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Runnable task;

    private SerialStartTask(Runnable task) {
      this.task = task;
    }

    @Override
    public void run() {
      latch.countDown();
      task.run();
    }

    public void waitForStart() throws InterruptedException {
      latch.await();
    }
  }
}
