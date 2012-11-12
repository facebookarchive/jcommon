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

import org.apache.zookeeper.KeeperException;

public interface LeaderElection extends LeaderElectionObserver {
  /**
   * Adds the current host to the election if it is not there already. May be
   * safely re-executed following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void enter() throws InterruptedException, KeeperException;

  /**
   * Completely removes the candidate from the election. May be safely
   * re-executed following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void withdraw() throws InterruptedException, KeeperException;

  /**
   * Relinquishes the candidate's current standing (possibly as the leader) and
   * demotes the candidate to the last position. May be safely re-executed
   * following previous exceptions.
   * @throws InterruptedException
   * @throws KeeperException
   */
  void cycle() throws InterruptedException, KeeperException;
}
