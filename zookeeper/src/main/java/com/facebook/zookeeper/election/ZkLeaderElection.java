package com.facebook.zookeeper.election;

import com.facebook.concurrency.ErrorLoggingRunnable;
import com.facebook.concurrency.NamedThreadFactory;
import com.facebook.zookeeper.Encodable;
import com.facebook.zookeeper.ZkUtil;
import com.facebook.zookeeper.ZooKeeperIface;
import com.facebook.zookeeper.connection.ZkConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A ZooKeeper primitive for handling non-blocking leader elections.
 *
 * This implementation tries to make no assumptions about the users of this
 * class and passes all error handling responsibilities to the callers.
 * On any unexpected errors, this class will not try to recover from
 * them. For example, if the candidate is unexpectedly removed from the
 * election, ZkLeaderElection will signal the removal, but make no attempt
 * to add them back into the election. It is up to the caller to decide whether
 * or not to re-enter the election.
 *
 * Note: All public methods are guaranteed to be idempotent and repeated
 * invocations will have no unintended effects. This might be a desired
 * course of action following an exception thrown on an earlier invocation.
 */
public class ZkLeaderElection implements LeaderElection {
  private static final Logger LOG = LoggerFactory.getLogger(ZkLeaderElection.class);
  
  private final ZkConnectionManager zkConnectionManager;
  private final PathFormat pathFormat;
  private final Candidate candidate;
  private final PredecessorMonitor predecessorMonitor = new PredecessorMonitor();
  private final LeaderElectionCallback leaderElectionCallback;
  private final ExecutorService watchExecutor;

  public ZkLeaderElection(
    ZkConnectionManager zkConnectionManager,
    String electionPath,
    String baseCandidateName,
    Encodable candidatePayload,
    LeaderElectionCallback leaderElectionCallback,
    ExecutorService watchExecutor
  ) {
    this.zkConnectionManager = zkConnectionManager;
    this.pathFormat = new PathFormat(electionPath, baseCandidateName);
    this.candidate = new Candidate(candidatePayload);
    this.leaderElectionCallback = leaderElectionCallback;
    this.watchExecutor = watchExecutor;
  }

  public ZkLeaderElection(
    ZkConnectionManager zkConnectionManager,
    String electionPath,
    String baseCandidateName,
    Encodable candidatePayload,
    LeaderElectionCallback leaderElectionCallback
  ) {
    this(
      zkConnectionManager,
      electionPath,
      baseCandidateName, 
      candidatePayload,
      leaderElectionCallback,
      Executors.newSingleThreadExecutor(
        new NamedThreadFactory("ZkLeaderElection-watch")
      )
    );
  }

  @Override
  public void enter() throws InterruptedException, KeeperException {
    candidate.enter();
  }

  @Override
  public void withdraw() throws InterruptedException, KeeperException {
    candidate.withdraw();
  }

  @Override
  public void cycle() throws InterruptedException, KeeperException {
    candidate.cycle();
  }

  @Override
  public String getLeader() throws InterruptedException, KeeperException { // TODO: add some way to watch for leader addition
    ZooKeeperIface zk = zkConnectionManager.getClient();
    List<String> candidateNames = getCandidateNames(zk);
    long leaderSeqNo = Long.MAX_VALUE;
    String leader = null;
    for (String candidateName : candidateNames) {
      long candidateSeqNo = pathFormat.extractSeqNo(candidateName);
      if (candidateSeqNo < leaderSeqNo) {
        leader = candidateName;
        leaderSeqNo = candidateSeqNo;
      }
    }
    return leader;
  }

  /**
   * Sets a watch on a node only if it exists. If the node does
   * not exist, no watch will be set.
   * @param zk
   * @param path
   * @param watcher
   * @return true if a watch was set (node exists), false otherwise
   * @throws InterruptedException
   * @throws KeeperException
   */
  private boolean setWatchIfNodeExists(
    ZooKeeperIface zk, String path, Watcher watcher
  ) throws InterruptedException, KeeperException {
    try {
      // Use getData instead of exists to set watch to guarantee that watch
      // will ONLY be set on an existing node. Otherwise you may get
      // watches set on non-existent nodes that will never be created
      // and thus will never fire, which is essentially a memory leak.
      zk.getData(path, watcher, null);
      return true;
    } catch (KeeperException.NoNodeException e) {
      return false;
    }
  }

  private String findCandidateName(long sessionId, List<String> candidateNames) {
    for (String candidateName : candidateNames) {
      if (sessionId == pathFormat.extractSessionId(candidateName)) {
        return candidateName;
      }
    }
    return null;
  }

  private List<String> getCandidateNames(ZooKeeperIface zk)
    throws InterruptedException, KeeperException {
    List<String> children = zk.getChildren(pathFormat.getElectionPath(), false);
    return pathFormat.filterByBaseName(children);
  }


  // Helper classes

  private class PathFormat {
    private static final char NAME_DELIM ='-';
    private final String electionPath;
    private final String baseCandidateName;

    private PathFormat(String electionPath, String baseCandidateName) {
      this.electionPath = electionPath;
      this.baseCandidateName = baseCandidateName;
    }

    public String getElectionPath() {
      return electionPath;
    }

    public String buildCandidatePrefix(long sessionId) {
      return baseCandidateName + NAME_DELIM + sessionId + NAME_DELIM;
    }

    public String buildPath(String candidateName) {
      return electionPath + "/" + candidateName;
    }

    public String buildCandidatePathPrefix(long sessionId) {
      return buildPath(buildCandidatePrefix(sessionId));
    }

    public long extractSessionId(String candidateName) {
      int startDelimIdx = candidateName.indexOf(NAME_DELIM);
      assert(startDelimIdx != -1);
      int endDelimIdx = candidateName.lastIndexOf(NAME_DELIM);
      assert(endDelimIdx != -1);
      assert(startDelimIdx != endDelimIdx);
      String sessionString =
        candidateName.substring(startDelimIdx+1, endDelimIdx);
      return Long.parseLong(sessionString);
    }

    public long extractSeqNo(String candidateName) {
      int delimIdx = candidateName.lastIndexOf(NAME_DELIM);
      assert(delimIdx != -1);
      String seqString = candidateName.substring(delimIdx+1);
      return Long.parseLong(seqString);
    }

    public List<String> filterByBaseName(List<String> nodes) {
      return ZkUtil.filterByPrefix(nodes, baseCandidateName + NAME_DELIM);
    }
  }

  private class Candidate {
    private final Encodable candidatePayload;
    private volatile CandidateWatcher currentCandidateWatcher = null;

    private Candidate(Encodable candidatePayload) {
      this.candidatePayload = candidatePayload;
    }

    public synchronized void enter()
      throws InterruptedException, KeeperException {
      ZooKeeperIface zk = zkConnectionManager.getClient();
      internalEnter(zk);
    }

    private void internalEnter(ZooKeeperIface zk)
      throws InterruptedException, KeeperException {
      String candidatePath = createCandidateNodeSafe(zk);
      if (currentCandidateWatcher == null ||
        !currentCandidateWatcher.appliesTo(candidatePath)) {
        currentCandidateWatcher = new CandidateWatcher(candidatePath);
      }

      if (!setWatchIfNodeExists(zk, candidatePath, currentCandidateWatcher)) {
        // Candidate must have already been removed before we could set watch      
        leaderElectionCallback.removed();
        return;
      }
      predecessorMonitor.monitor(zk);
      
      LOG.info("entering election for path " + candidatePath);
    }

    public synchronized void withdraw()
      throws InterruptedException, KeeperException {
      ZooKeeperIface zk = zkConnectionManager.getClient();
      internalWithdraw(zk);
    }

    private void internalWithdraw(ZooKeeperIface zk)
      throws InterruptedException, KeeperException {
      if (currentCandidateWatcher != null) {
        // Suppress the callback that will be triggered by delete
        // OK if something else deletes the node first
        currentCandidateWatcher.ignoreOneDelete();
      }
      List<String> candidateNames = getCandidateNames(zk);
      String candidateName = findCandidateName(zk.getSessionId(), candidateNames);
      if (candidateName != null) {
        try {
          String path = pathFormat.buildPath(candidateName);
          zk.delete(path, -1);
          LOG.info("withdrawing for path " + path);
        } catch (KeeperException.NoNodeException e) {
          if (currentCandidateWatcher != null) {
            // Ignore if the candidate was already deleted
            currentCandidateWatcher.unsetIgnoreOneDelete();
          }
        }
      }
    }

    public synchronized void cycle()
      throws InterruptedException, KeeperException {
      ZooKeeperIface zk = zkConnectionManager.getClient();
      internalWithdraw(zk);
      internalEnter(zk);
    }

    // NOTE: this entire method needs to be synchronized, and is currently
    // protected by the synchronization of enter().
    private String createCandidateNodeSafe(ZooKeeperIface zk)
      throws InterruptedException, KeeperException {
      // Check if the candidate has already entered the election
      List<String> candidateNames = getCandidateNames(zk);
      String candidateName = findCandidateName(zk.getSessionId(), candidateNames);
      if (candidateName != null) {
        // Candidate already exists so just return its path
        return pathFormat.buildPath(candidateName);
      }
      // Add the candidate and return its new path
      return zk.create(
        pathFormat.buildCandidatePathPrefix(zk.getSessionId()),
        candidatePayload.encode(),
        ZooDefs.Ids.OPEN_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL
      );
    }

    private class CandidateWatcher implements Watcher {
      private final String candidatePath;
      private volatile boolean ignoreOneDelete = false;

      private CandidateWatcher(String candidatePath) {
        this.candidatePath = candidatePath;
      }

      public boolean appliesTo(String path) {
        return candidatePath.equals(path);
      }

      public void ignoreOneDelete() {
        ignoreOneDelete = true;
      }

      public void unsetIgnoreOneDelete() {
        ignoreOneDelete = false;
      }

      @Override
      public void process(final WatchedEvent event) {
        watchExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
          @Override
          public void run() {
            switch (event.getState()) {
              case SyncConnected:
                processNodeEvent(event.getType());
                break;
              case Expired:
                // All public methods guaranteed to fail on session expiration
                // and candidate will be removed
                leaderElectionCallback.removed();
                break;
            }
          }
        }));
      }

      private void processNodeEvent(Event.EventType eventType) {
        switch (eventType) {
          case NodeDeleted:
            // No locking since process() should be single-threaded
            if (!ignoreOneDelete) {
              leaderElectionCallback.removed();
            }
            ignoreOneDelete = false;
            break;

          case NodeChildrenChanged:
          case NodeCreated:
          case NodeDataChanged:
            // Some irrelevant node event triggered the watch, need to re-set it
            try {
              ZooKeeperIface zk = zkConnectionManager.getClient();
              if (!setWatchIfNodeExists(zk, candidatePath, this)) {
                leaderElectionCallback.removed();
              }
            } catch (Exception e) {
              leaderElectionCallback.error(e);
            }
            break;
        }
      }
    }
  }

  private class PredecessorMonitor {
    private final PredecessorWatcher predecessorWatcher =
      new PredecessorWatcher();

    public void monitor(ZooKeeperIface zk)
      throws InterruptedException, KeeperException {
      // This will eventually terminate because we only loop if a predecessor
      // is deleted before a watch can be set and there is a finite number of
      // predecessors for the current candidate.
      while (true) {
        // Refresh the list of candidates to include the current candidate.
        // We need to do this to solidify the relative ordering of candidates
        // with our current one.
        List<String> candidateNames = getCandidateNames(zk);
        if (findCandidateName(zk.getSessionId(), candidateNames) == null) {
          // Candidate must have been removed from the election
          // Ignore this case and let the candidate signal the deletion
          break;
        }
        String predecessor =
          findPrecedingCandidateName(zk.getSessionId(), candidateNames);
        if (predecessor == null) {
          // No predecessor means that we are the leader
          leaderElectionCallback.elected();
          break;
        }
        String predecessorPath = pathFormat.buildPath(predecessor);
        if (setWatchIfNodeExists(zk, predecessorPath, predecessorWatcher)) {
          // Done if we successfully set watch on our predecessor
          break;
        }
        // Need to repeat if the predecessor was deleted before watch was set
      }
    }

    /**
     * Finds the name of the candidate directly preceding the one associated
     * with the session id. Returns null if it is the current leader.
     * Precondition: candidate associated with session id must be in the list of
     * candidate names.
     * @param sessionId
     * @param candidateNames
     * @return Name of the closest preceding candidate, or null if the candidate
     * associated with the session id is the leader
     */
    private String findPrecedingCandidateName(
      long sessionId, List<String> candidateNames
    ) {
      String thisCandidateName = findCandidateName(sessionId, candidateNames);
      assert(thisCandidateName != null); // This candidate must exist in list
      long thisSeqNo = pathFormat.extractSeqNo(thisCandidateName);
      long closestPrecedingSeqNo = -1;
      String closestPrecedingCandidateName = null;
      for (String candidateName : candidateNames) {
        long candidateSeqNo = pathFormat.extractSeqNo(candidateName);
        if (candidateSeqNo < thisSeqNo && candidateSeqNo > closestPrecedingSeqNo) {
          closestPrecedingSeqNo = candidateSeqNo;
          closestPrecedingCandidateName = candidateName;
        }
      }
      return closestPrecedingCandidateName;
    }

    private class PredecessorWatcher implements Watcher {
      @Override
      public void process(final WatchedEvent event) {
        watchExecutor.execute(new ErrorLoggingRunnable(new Runnable() {
          @Override
          public void run() {
            if (event.getType() != Event.EventType.None) {
              // Check for changes in predecessor and re-set watch if necessary.
              try {
                ZooKeeperIface zk = zkConnectionManager.getClient();
                monitor(zk);
              } catch (Exception e) {
                leaderElectionCallback.error(e);
              }
            }
          }
        }));
      }
    }


  }
}
