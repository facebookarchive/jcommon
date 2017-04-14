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
package com.facebook.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

/**
 * Runtime.addShutdownHook() has no guarantee of order.  This class
 * will run hooks by stage and in the order added within stages
 */
public class ShutdownManagerImpl<T extends Enum> implements ShutdownManager<T> {
  private static final Logger LOG = LoggerImpl.getLogger(ShutdownManagerImpl.class);

  private final Map<T, List<Runnable>> shutdownHooksByStage = new ConcurrentHashMap<>();
  private final Thread thread;
  private final Object shutdownLock = new Object();
  private final T[] stages;
  private final T firstStage;
  private final T lastStage;
  private final T defaultStage;

  private T currentStage;
  private boolean isShutdown = false;

  public ShutdownManagerImpl(Class<T> enumClazz, T defaultStage) {
    this.defaultStage = defaultStage;

    if (!enumClazz.isEnum()) {
      throw new IllegalArgumentException(
        String.format(
          "%s is not an enum class", enumClazz.getName()
        )
      );
    }

    stages = enumClazz.getEnumConstants();
    for (T stage : stages) {
      shutdownHooksByStage.put(stage, new ArrayList<Runnable>());
    }

    // three values : being/end sentinel and at least one usable value
    if (stages.length < 3) {
      throw new IllegalArgumentException("enum class must have at least 3 values");
    }

    firstStage = stages[0];
    currentStage = firstStage;
    lastStage = stages[stages.length - 1];
    thread = new Thread(
      new Runnable() {
        @Override
        public void run() {
          internalShutdown();
        }
      }
    );
  }

  @Override
  public boolean tryAddShutdownHook(Runnable hook) {
    return tryAddShutdownHook(defaultStage, hook);
  }

  @Override
  public boolean tryAddShutdownHook(T stage, Runnable hook) {
    if (stage == firstStage || stage == lastStage) {
      throw new IllegalArgumentException(
        String.format("stage %s is reserved", stage)
      );
    }

    synchronized (shutdownLock) {
      //if the stage being added is our stage or earlier, we can't accept this
      if (stage.compareTo(currentStage) <= 0) {
        LOG.warn(
            "cannot add hook for stage %s when in stage %s",
            stage,
            currentStage
        );
        return false;
      }

      shutdownHooksByStage.get(stage).add(hook);

      return true;
    }
  }

  @Override
  public void addShutdownHook(Runnable hook) {
    addShutdownHook(defaultStage, hook);
  }

  @Override
  public void addShutdownHook(T stage, Runnable hook) {
    if (!tryAddShutdownHook(stage, hook)) {
      throw new IllegalStateException(
        "trying to add a hook after shutdown started"
      );
    }
  }

  @Override
  public void shutdown() {
    if (internalShutdown()) {
      // fb303.shutdown calls this, so remove the shutdown hook after we're done
      Runtime.getRuntime().removeShutdownHook(thread);
    }
  }

  @Override
  public Thread getAsThread() {
    return thread;
  }

  /**
   * @return true if this executed the shutdown
   */
  private boolean internalShutdown() {
    synchronized (shutdownLock) {
      if (isShutdown) {
        LOG.info("ignoring extra shutdown call");

        return false;
      }

      isShutdown = true;
    }

    LOG.info("starting service shutdown hooks");

    for (T stage : stages) {
      LOG.info("starting stage " + stage);
      synchronized (shutdownLock) {
        currentStage = stage;
      }

      for (Runnable hook : shutdownHooksByStage.get(stage)) {
        try {
          hook.run();
        } catch (Throwable t) {
          LOG.warn("error running hook", t);
        }
      }

      LOG.info("ending stage " + stage);
    }

    LOG.info("shutdown complete");

    return true;
  }
}
