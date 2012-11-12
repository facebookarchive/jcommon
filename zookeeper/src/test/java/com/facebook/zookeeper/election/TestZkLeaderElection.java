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

import com.facebook.testing.MockExecutor;
import com.facebook.zookeeper.mock.MockWatcher;
import com.facebook.zookeeper.mock.MockZkConnectionManager;
import com.facebook.zookeeper.mock.MockZooKeeper;
import com.facebook.zookeeper.mock.MockZooKeeperDataStore;
import com.facebook.zookeeper.mock.MockZooKeeperFactory;
import com.facebook.zookeeper.VariablePayload;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class TestZkLeaderElection {
  private static final String testData1 = "testData1";
  private static final String testData2 = "testData2";
  private static final String testData3 = "testData3";
  private static final String electionRoot = "/root/work/election";
  private static final String candidateBaseName = "candidate";
  private MockZooKeeperDataStore dataStore;
  private MockZooKeeperFactory mockZooKeeperFactory;
  private MockZkConnectionManager mockZkConnectionManager1;
  private MockZkConnectionManager mockZkConnectionManager2;
  private MockZkConnectionManager mockZkConnectionManager3;
  private ZkLeaderElection zkLeaderElection1;
  private ZkLeaderElection zkLeaderElection2;
  private ZkLeaderElection zkLeaderElection3;
  private MockLeaderElectionCallback mockLeaderElectionCallback1;
  private MockLeaderElectionCallback mockLeaderElectionCallback2;
  private MockLeaderElectionCallback mockLeaderElectionCallback3;
  private VariablePayload variablePayload1;
  private VariablePayload variablePayload2;
  private VariablePayload variablePayload3;
  private MockExecutor mockExecutor1;
  private MockExecutor mockExecutor2;
  private MockExecutor mockExecutor3;
  private MockZooKeeper zk1;
  private MockZooKeeper zk2;
  private MockZooKeeper zk3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    dataStore = new MockZooKeeperDataStore();
    mockZooKeeperFactory = new MockZooKeeperFactory(dataStore);
    mockZkConnectionManager1 = new MockZkConnectionManager(mockZooKeeperFactory);
    mockZkConnectionManager1.refreshClient(); // Generate a new client
    zk1 = mockZooKeeperFactory.getLastZooKeeper();
    zk1.triggerConnect();
    mockZkConnectionManager2 = new MockZkConnectionManager(mockZooKeeperFactory);
    mockZkConnectionManager2.refreshClient(); // Generate a new client
    zk2 = mockZooKeeperFactory.getLastZooKeeper();
    zk2.triggerConnect();
    mockZkConnectionManager3 = new MockZkConnectionManager(mockZooKeeperFactory);
    mockZkConnectionManager3.refreshClient(); // Generate a new client
    zk3 = mockZooKeeperFactory.getLastZooKeeper();
    zk3.triggerConnect();

    // Create ZkLeaderElection1
    mockExecutor1 = new MockExecutor();
    variablePayload1 = new VariablePayload(testData1);
    mockLeaderElectionCallback1 = new MockLeaderElectionCallback();
    zkLeaderElection1 =
      new ZkLeaderElection(
        mockZkConnectionManager1,
        electionRoot,
        candidateBaseName,
        variablePayload1,
        mockLeaderElectionCallback1,
        mockExecutor1
      );

    // Create ZkLeaderElection2
    mockExecutor2 = new MockExecutor();
    variablePayload2 = new VariablePayload(testData2);
    mockLeaderElectionCallback2 = new MockLeaderElectionCallback();
    zkLeaderElection2 =
      new ZkLeaderElection(
        mockZkConnectionManager2,
        electionRoot,
        candidateBaseName,
        variablePayload2,
        mockLeaderElectionCallback2,
        mockExecutor2      
      );

    // Create ZkLeaderElection3
    mockExecutor3 = new MockExecutor();
    variablePayload3 = new VariablePayload(testData3);
    mockLeaderElectionCallback3 = new MockLeaderElectionCallback();
    zkLeaderElection3 =
      new ZkLeaderElection(
        mockZkConnectionManager3,
        electionRoot,
        candidateBaseName,
        variablePayload3,
        mockLeaderElectionCallback3,
        mockExecutor3
      );

    // Set up election root
    zk1.create("/root", null, null, CreateMode.PERSISTENT);
    zk1.create("/root/work", null, null, CreateMode.PERSISTENT);
    zk1.create("/root/work/election", null, null, CreateMode.PERSISTENT);
  }

  @Test(groups = "fast", expectedExceptions = KeeperException.ConnectionLossException.class)
  public void testNoConnection() throws Exception {
    zk1.triggerDisconnect();
    zkLeaderElection1.enter();
  }

  @Test(groups = "fast", expectedExceptions = KeeperException.NoNodeException.class)
  public void testMissingRoot() throws Exception {
    zk1.delete(electionRoot, -1);
    zkLeaderElection1.enter();
  }

  @Test(groups = "fast")
  public void testEnter() throws Exception {
    // Verify no candidates
    MockWatcher mockWatcher = new MockWatcher();
    List<String> candidates = zk1.getChildren(electionRoot, mockWatcher);
    Assert.assertTrue(candidates.isEmpty());

    // Enter the candidate and run any callbacks
    zkLeaderElection1.enter();
    mockExecutor1.drain();

    // Check that candidate is created
    candidates = zk1.getChildren(electionRoot, null);
    Assert.assertEquals(candidates.size(), 1);

    // Check that candidate is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();
    Assert.assertEquals(zkLeaderElection1.getLeader(), candidates.get(0));

    // Check data contents
    String data = VariablePayload.decode(
      zk1.getData(electionRoot + "/" + candidates.get(0), null, null)
    );
    Assert.assertEquals(data, testData1);

    // Check that external watch was notified of candidate creation
    Assert.assertEquals(mockWatcher.getEventQueue().size(), 1);
    WatchedEvent watchedEvent = mockWatcher.getEventQueue().remove();
    Assert.assertEquals(watchedEvent.getType(), EventType.NodeChildrenChanged);
    Assert.assertEquals(watchedEvent.getPath(), electionRoot);
  }

  @Test(groups = "fast")
  public void testMultipleEnter() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate is notified of election win again
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    // Only one candidate should be entered
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 1);
  }

  @Test(groups = "fast")
  public void testPrematureWithdraw() throws Exception {
    // No exception should be thrown if the candidate does not exist
    zkLeaderElection1.withdraw();
    mockExecutor1.drain();
  }

  @Test(groups = "fast")
  public void testWithdraw() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();

    // Verify candidate
    MockWatcher mockWatcher = new MockWatcher();
    List<String> candidates = zk1.getChildren(electionRoot, mockWatcher);
    Assert.assertEquals(candidates.size(), 1);
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection1.withdraw();
    mockExecutor1.drain();

    candidates = zk1.getChildren(electionRoot, null);
    Assert.assertTrue(candidates.isEmpty());
    Assert.assertEquals(zkLeaderElection1.getLeader(), null);
    // Manual withdraw should not trigger a "removed" callback
    Assert.assertFalse(mockLeaderElectionCallback1.isRemoved());
    Assert.assertEquals(mockWatcher.getEventQueue().size(), 1);
    WatchedEvent watchedEvent = mockWatcher.getEventQueue().remove();
    Assert.assertEquals(watchedEvent.getType(), EventType.NodeChildrenChanged);
    Assert.assertEquals(watchedEvent.getPath(), electionRoot);
  }

  @Test(groups = "fast")
  public void testMultipleWithdraw() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();

    zkLeaderElection1.withdraw();
    mockExecutor1.drain();
    // Check candidate is removed
    Assert.assertTrue(zk1.getChildren(electionRoot, null).isEmpty());

    zkLeaderElection1.withdraw();
    mockExecutor1.drain();
    // Check that there is still no candidate
    Assert.assertTrue(zk1.getChildren(electionRoot, null).isEmpty());
  }

  @Test(groups = "fast")
  public void testCycle() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection1.cycle();
    mockExecutor1.drain();
    // Check that candidate is re-elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();
    // Check no remove was signaled
    Assert.assertFalse(mockLeaderElectionCallback1.isRemoved());
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 1);
  }

  @Test(groups = "fast")
  public void testEarlyCycle() throws Exception {
    zkLeaderElection1.cycle();
    mockExecutor1.drain();
    // Check that candidate is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();
    // Check no remove was signaled
    Assert.assertFalse(mockLeaderElectionCallback1.isRemoved());
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 1);
  }

  @Test(groups = "fast")
  public void testMultiEnter() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate1 is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection2.enter();
    mockExecutor2.drain();
    // Check that candidate2 was not elected
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());

    // Check that both candidates are in the election
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 2);
    // Check that they both agree on the winner
    Assert.assertEquals(zkLeaderElection1.getLeader(), zkLeaderElection2.getLeader());
    // Check that the reported winner is candidate1
    Assert.assertEquals(
      VariablePayload.decode(zk1.getData(
        electionRoot + "/" + zkLeaderElection1.getLeader(), null, null
      )),
      testData1
    );
  }

  @Test(groups = "fast")
  public void testMultiCycle() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate1 is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection2.enter();
    mockExecutor2.drain();
    // Check that candidate2 was not elected
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());

    zkLeaderElection1.cycle();
    mockExecutor1.drain();
    mockExecutor2.drain();
    // Check that candidate2 was elected and not candidate1
    Assert.assertTrue(mockLeaderElectionCallback2.isElected());
    mockLeaderElectionCallback2.resetElected();
    Assert.assertFalse(mockLeaderElectionCallback1.isElected());

    // Check that both candidates are in the election
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 2);
    // Check that they both agree on the winner
    Assert.assertEquals(zkLeaderElection1.getLeader(), zkLeaderElection2.getLeader());
    // Check that the reported winner is candidate2
    Assert.assertEquals(
      VariablePayload.decode(zk1.getData(
        electionRoot + "/" + zkLeaderElection1.getLeader(), null, null
      )),
      testData2
    );
  }

  @Test(groups = "fast")
  public void testMultiWithExpire() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate1 is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection2.enter();
    mockExecutor2.drain();
    // Check that candidate2 was not elected
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());

    zk1.triggerSessionExpiration();
    mockExecutor1.drain();
    mockExecutor2.drain();
    // Check that candidate2 was elected
    Assert.assertTrue(mockLeaderElectionCallback2.isElected());
    mockLeaderElectionCallback2.resetElected();

    // Check that only candidate2 is in the election
    Assert.assertEquals(zk2.getChildren(electionRoot, null).size(), 1);
    // Check that the reported winner is candidate2
    Assert.assertEquals(
      VariablePayload.decode(zk2.getData(
        electionRoot + "/" + zkLeaderElection2.getLeader(), null, null
      )),
      testData2
    );
  }

  @Test(groups = "fast")
  public void testAdminRemove() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate1 is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection2.enter();
    mockExecutor2.drain();
    // Check that candidate2 was not elected
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());

    // Delete candidate1 via external forces
    zk1.delete(electionRoot + "/" + zkLeaderElection1.getLeader(), -1);
    mockExecutor1.drain();
    mockExecutor2.drain();
    //Check that candidate1 got a removed signal
    Assert.assertTrue(mockLeaderElectionCallback1.isRemoved());
    mockLeaderElectionCallback1.resetRemoved();
    // Check that candidate2 was elected
    Assert.assertTrue(mockLeaderElectionCallback2.isElected());
    mockLeaderElectionCallback2.resetElected();

    // Check that only candidate2 is in the election
    Assert.assertEquals(zk2.getChildren(electionRoot, null).size(), 1);
    // Check that the reported winner is candidate2
    Assert.assertEquals(
      VariablePayload.decode(zk2.getData(
        electionRoot + "/" + zkLeaderElection2.getLeader(), null, null
      )),
      testData2
    );
  }

  @Test(groups = "fast")
  public void testMultiSuccession() throws Exception {
    zkLeaderElection1.enter();
    mockExecutor1.drain();
    // Check that candidate1 is elected
    Assert.assertTrue(mockLeaderElectionCallback1.isElected());
    mockLeaderElectionCallback1.resetElected();

    zkLeaderElection2.enter();
    mockExecutor2.drain();
    // Check that candidate2 was not elected
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());

    zkLeaderElection3.enter();
    mockExecutor3.drain();
    // Check that candidate3 was not elected
    Assert.assertFalse(mockLeaderElectionCallback3.isElected());

    // Check that all candidates are in the election
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 3);
    // Check that they all agree on the winner
    Assert.assertEquals(zkLeaderElection1.getLeader(), zkLeaderElection2.getLeader());
    Assert.assertEquals(zkLeaderElection2.getLeader(), zkLeaderElection3.getLeader());

    zkLeaderElection2.withdraw();
    mockExecutor2.drain();
    mockExecutor3.drain();
    // Check that no election callbacks happened
    Assert.assertFalse(mockLeaderElectionCallback1.isElected());
    Assert.assertFalse(mockLeaderElectionCallback2.isElected());
    Assert.assertFalse(mockLeaderElectionCallback3.isElected());

    // Check that there are now two candidates in the election
    Assert.assertEquals(zk1.getChildren(electionRoot, null).size(), 2);

    zkLeaderElection1.withdraw();
    mockExecutor1.drain();
    mockExecutor3.drain();
    // Check that candidate3 got the election callback
    Assert.assertTrue(mockLeaderElectionCallback3.isElected());
    mockLeaderElectionCallback3.resetElected();

    // Check that the reported winner is candidate3
    Assert.assertEquals(
      VariablePayload.decode(zk1.getData(
        electionRoot + "/" + zkLeaderElection1.getLeader(), null, null
      )),
      testData3
    );
  }
}
