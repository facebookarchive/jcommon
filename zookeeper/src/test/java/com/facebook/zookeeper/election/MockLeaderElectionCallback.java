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
