package com.facebook.zookeeper.election;

public class MockLeaderElectionCallback implements LeaderElectionCallback {
  private boolean isElected = false;
  private boolean isRemoved = false;
  private Exception exception = null;

  public boolean isElected() {
    return isElected;
  }

  public void resetElected() {
    isElected = false;
  }

  public boolean isRemoved() {
    return isRemoved;
  }

  public void resetRemoved() {
    isRemoved = false;
  }

  public Exception getException() {
    return exception;
  }

  public void resetException() {
    exception = null;
  }

  @Override
  public void elected() {
    isElected = true;
  }

  @Override
  public void removed() {
    isRemoved = true;
  }

  @Override
  public void error(Exception exception) {
    this.exception = exception;
  }
}
