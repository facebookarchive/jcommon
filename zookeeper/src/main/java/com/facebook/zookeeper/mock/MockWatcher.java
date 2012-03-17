package com.facebook.zookeeper.mock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.LinkedList;
import java.util.Queue;

public class MockWatcher implements Watcher {
  private Queue<WatchedEvent> eventQueue = new LinkedList<WatchedEvent>();

  public Queue<WatchedEvent> getEventQueue() {
    return eventQueue;
  }

  @Override
  public void process(WatchedEvent event) {
    eventQueue.offer(event);
  }
}
