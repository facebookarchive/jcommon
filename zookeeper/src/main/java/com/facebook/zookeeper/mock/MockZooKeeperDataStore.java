package com.facebook.zookeeper.mock;


import com.facebook.collections.RetrieveableSet;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.DataTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// TODO: what actions trigger version increments?
public class MockZooKeeperDataStore {
  private final AtomicLong nextSessionId = new AtomicLong(0);
  private final ZNode root = ZNode.createRoot();
  private final Map<String, RetrieveableSet<ContextedWatcher>> creationWatchers =
    new HashMap<String, RetrieveableSet<ContextedWatcher>>();

  public long getUniqueSessionId() {
    return nextSessionId.addAndGet(1);
  }

  public synchronized void signalSessionEvent(long sessionId, WatchedEvent watchedEvent) {
    for (RetrieveableSet<ContextedWatcher> pathWatchers : creationWatchers.values()) {
      for (ContextedWatcher contextedWatcher : pathWatchers) {
        if (contextedWatcher.getSessionId() == sessionId) {
          contextedWatcher.process(watchedEvent);
        }
      }
    }
    for (ZNode zNode : root) {
      zNode.signalSessionEvent(sessionId, watchedEvent);
    }
  }

  public synchronized void clearSession(long sessionId) {
    for (RetrieveableSet<ContextedWatcher> pathWatchers : creationWatchers.values()) {
      Iterator<ContextedWatcher> iter = pathWatchers.iterator();
      while (iter.hasNext()) {
        ContextedWatcher contextedWatcher = iter.next();
        if (contextedWatcher.getSessionId() == sessionId) {
          iter.remove();
        }
      }
    }
    for (ZNode zNode : root) {
      zNode.clearSession(sessionId);
    }
  }

  public synchronized String create(
    long sessionId, String path, byte[] data, List<ACL> acl, CreateMode createMode
  ) throws KeeperException {
    if (isRootPath(path)) {
      throw new KeeperException.NodeExistsException(path);
    }
    String relativePath = stripRootFromPath(path);
    String relativeChildPath =
      root.createDescendant(
        sessionId, relativePath, data, acl, createMode
      );
    String absChildPath = addRootToPath(relativeChildPath);

    // Trigger any creation watches that may exist
    if (creationWatchers.containsKey(absChildPath)) {
      WatchedEvent watchedEvent =
        new WatchedEvent(
          EventType.NodeCreated,
          KeeperState.SyncConnected,
          absChildPath
        );
      for (Watcher watcher : creationWatchers.get(absChildPath)) {
        watcher.process(watchedEvent);
      }
      creationWatchers.remove(absChildPath);
    }
    return absChildPath;
  }

  public synchronized void delete(String path, int expectedVersion) throws KeeperException {
    if (isRootPath(path)) {
      throw new KeeperException.BadArgumentsException(path);
    }
    String relativePath = stripRootFromPath(path);
    root.deleteDescendant(relativePath, expectedVersion);
  }

  public synchronized Stat exists(long sessionId, String path, Watcher watcher)
    throws KeeperException {
    try {
      ZNode node =
        isRootPath(path) ? root : root.findDescendant(stripRootFromPath(path));
      if (watcher != null) {
        node.addWatcher(sessionId, watcher, WatchTriggerPolicy.WatchType.EXISTS);
      }
      Stat stat = new Stat();
      DataTree.copyStat(node.getStat(), stat);
      return stat;
    } catch (KeeperException.NoNodeException e) {
      if (watcher != null) {
        // Set a watch for this node when it gets created
        if (!creationWatchers.containsKey(path)) {
          creationWatchers.put(path, new RetrieveableSet<ContextedWatcher>());
        }
        ContextedWatcher contextedWatcher =
          new ContextedWatcher(
            watcher,
            sessionId,
            WatchTriggerPolicy.WatchType.EXISTS
          );
        if (!creationWatchers.get(path).contains(contextedWatcher)) {
          creationWatchers.get(path).add(contextedWatcher);
        }
      }
      return null;
    }
  }

  public synchronized byte[] getData(long sessionId, String path, Watcher watcher, Stat stat)
    throws KeeperException {
    ZNode node =
      isRootPath(path) ? root : root.findDescendant(stripRootFromPath(path));
    if (watcher != null) {
      node.addWatcher(sessionId, watcher, WatchTriggerPolicy.WatchType.GETDATA);
    }
    if (stat != null) {
      DataTree.copyStat(node.getStat(), stat);
    }
    return node.getData();
  }

  public synchronized Stat setData(String path, byte[] data, int expectedVersion)
    throws KeeperException {
    ZNode node =
      isRootPath(path) ? root : root.findDescendant(stripRootFromPath(path));
    node.setData(data, expectedVersion);
    Stat stat = new Stat();
    DataTree.copyStat(node.getStat(), stat);
    return stat;
  }

  public synchronized List<String> getChildren(long sessionId, String path, Watcher watcher)
    throws KeeperException {
    ZNode node =
      isRootPath(path) ? root : root.findDescendant(stripRootFromPath(path));
    if (watcher != null) {
      node.addWatcher(sessionId, watcher, WatchTriggerPolicy.WatchType.GETCHILDREN);
    }
    return new ArrayList<String>(node.getChildren().keySet());
  }

  private static boolean isRootPath(String path) {
    return path.equals("/");
  }

  private static String stripRootFromPath(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("Does not have root: " + path);
    }
    // Remove the leading slash for the root node
    return path.substring(1);
  }

  private static String addRootToPath(String path) {
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Already has root: " + path);
    }
    // Add the leading slash for the root node
    return "/" + path;
  }

  /**
   * ZNode: basic node storage unit. Collectively, they form the mock ZooKeeper
   * data storage tree hierarchy.
   *
   * For each ZNode:
   * - Contains basic tree traversal algorithms stemming from the current ZNode
   * - Maintains and signals watches set on the node
   * - Capable of iterating across its entire sub-tree
   *
   * Assumptions:
   * - All paths will be specified relative to the current node. For example,
   * given the following tree:
   *                                  A
   *                                /  \
   *                              B     C
   *                            /  \
   *                          D     E
   *                        /
   *                      F
   *
   * If the current node is A, the path we specify to reach F will be: "B/D/F"
   * If the current node is B, the path we specify to reach F will be: "D/F"
   * Note: paths should never start or end with a '/'
   */
  private static class ZNode implements Iterable<ZNode> {
    private final ZNode parent;
    private final String name;
    private byte[] data;
    private List<ACL> acl;
    private final CreateMode createMode;
    private final Stat stat = new Stat();
    private final AtomicLong nextSeqNum = new AtomicLong(0);
    private final AtomicInteger version = new AtomicInteger(0);
    private final Map<String, ZNode> children = new HashMap<String, ZNode>();
    private final RetrieveableSet<ContextedWatcher> contextedWatchers =
      new RetrieveableSet<ContextedWatcher>();

    private ZNode(
      long sessionId,
      ZNode parent,
      String name,
      byte[] data,
      List<ACL> acl,
      CreateMode createMode
    ) {
      this.parent = parent;
      this.name = name;
      this.data = data;
      this.acl = acl;
      this.createMode = createMode;
      stat.setEphemeralOwner(createMode.isEphemeral() ? sessionId : 0);
      stat.setDataLength((data == null) ? 0 : data.length);
      stat.setNumChildren(0);
      stat.setVersion(version.get());
    }

    public static ZNode createRoot() {
      return new ZNode(0, null, "", new byte[0], null, CreateMode.PERSISTENT);
    }

    public void addWatcher(
      long sessionId, Watcher watcher, WatchTriggerPolicy.WatchType watchType
    ) {
      ContextedWatcher contextedWatcher =
        new ContextedWatcher(watcher, sessionId, watchType);
      if (contextedWatchers.contains(contextedWatcher)) {
        contextedWatchers.get(contextedWatcher).merge(contextedWatcher);
      } else {
        contextedWatchers.add(contextedWatcher);
      }
    }

    public void clearSession(long sessionId) {
      // First remove all of your own watches
      Iterator<ContextedWatcher> iter = contextedWatchers.iterator();
      while(iter.hasNext()) {
        if (iter.next().getSessionId() == sessionId) {
          iter.remove();
        }
      }
      // Delete self if node is ephemeral
      if (stat.getEphemeralOwner() == sessionId) {
        try {
          delete(-1);
        } catch (KeeperException e) {
          throw new RuntimeException(e);
        }
      }
      // This session should not receive any callbacks as a result of clearing
    }

    public void signalSessionEvent(long sessionId, WatchedEvent watchedEvent) {
      for (ContextedWatcher contextedWatcher : contextedWatchers) {
        if (contextedWatcher.getSessionId() == sessionId) {
          contextedWatcher.process(watchedEvent);
        }
      }
    }

    public void signalNodeEvent(EventType eventType) {
      assert(eventType != EventType.None);
      WatchedEvent watchedEvent =
        new WatchedEvent(
          eventType,
          KeeperState.SyncConnected,
          addRootToPath(getPath())
        );
      Iterator<ContextedWatcher> iter = contextedWatchers.iterator();
      while(iter.hasNext()) {
        ContextedWatcher contextedWatcher = iter.next();
        if (contextedWatcher.shouldTrigger(eventType)) {
          iter.remove(); // Remove for one use
          contextedWatcher.process(watchedEvent);
        }
      }
    }

    public ZNode findDescendant(String path) throws KeeperException {
      List<String> pathParts = Arrays.asList(path.split("/"));
      ZNode lastSeenZNode = this;
      for (String childName : pathParts) {
        lastSeenZNode = lastSeenZNode.getChildren().get(childName);
        if (lastSeenZNode == null) {
          throw new KeeperException.NoNodeException();
        }
      }
      return lastSeenZNode;
    }

    public ZNode findLeafParent(String path) throws KeeperException {
      if (!path.contains("/")) {
        // No slashes => this must be the parent
        return this;
      }
      return findDescendant(getLeafParentPath(path));
    }

    private static String getLeafParentPath(String path) {
      int idx = path.lastIndexOf("/");
      if (idx == -1) {
        throw new IllegalArgumentException("Path does not have parent: " + path);
      }
      return path.substring(0, idx);
    }

    public String getPath() {
      ZNode currentNode = this;
      String path = "";
      while (!currentNode.isRoot()) {
        if (!path.isEmpty()) {
          path = "/" + path;
        }
        path = currentNode.getName() + path;
        currentNode = currentNode.getParent();
      }
      return path;
    }

    private static String getLeafName(String path) {
      int idx = path.lastIndexOf("/");
      if (idx == -1) {
        return path;
      }
      return path.substring(idx+1);
    }

    public String createDescendant(
      long sessionId,
      String path,
      byte[] data,
      List<ACL> acl,
      CreateMode createMode
    ) throws KeeperException {
      ZNode parent = findLeafParent(path);
      String childName =
        parent.createChild(sessionId, getLeafName(path), data, acl, createMode);
      return parent.isRoot() ? childName : parent.getPath() + "/" + childName;
    }

    public String createChild(
      long sessionId,
      String childName,
      byte[] data,
      List<ACL> acl,
      CreateMode createMode
    ) throws KeeperException {
      // Append a sequence number to path if sequential
      if (createMode.isSequential()) {
        childName += String.format("%08d", nextSeqNum.addAndGet(1));
      }
      ZNode zNode = new ZNode(sessionId, this, childName, data, acl, createMode);
      addChild(zNode);
      zNode.signalNodeEvent(EventType.NodeCreated);
      return childName;
    }

    public void addChild(ZNode zNode) throws KeeperException {
      if (createMode.isEphemeral()) {
        throw new KeeperException.NoChildrenForEphemeralsException();
      }
      if (children.containsKey(zNode.getName())) {
        throw new KeeperException.NodeExistsException();
      }
      children.put(zNode.getName(), zNode);
      stat.setNumChildren(children.size());

      signalNodeEvent(EventType.NodeChildrenChanged);
    }

    public void deleteDescendant(String path, int expectedVersion)
      throws KeeperException {
      findDescendant(path).delete(expectedVersion);
    }

    public void delete(int expectedVersion) throws KeeperException {
      assert(!isRoot());
      if (!getChildren().isEmpty()) {
        throw new KeeperException.NotEmptyException();
      }
      if (expectedVersion != -1 && getStat().getVersion() != expectedVersion) {
        throw new KeeperException.BadVersionException();
      }
      if (getParent().children.remove(getName()) == null) {
        throw new KeeperException.NoNodeException();
      }

      signalNodeEvent(EventType.NodeDeleted);
      getParent().signalNodeEvent(EventType.NodeChildrenChanged);
    }

    public boolean isRoot() {
      return parent == null;
    }

    public ZNode getParent() {
      return parent;
    }

    public String getName() {
      return name;
    }

    public byte[] getData() {
      return data;
    }

    public void setData(byte[] newData, int expectedVersion)
      throws KeeperException {
      if (expectedVersion != -1 && getStat().getVersion() != expectedVersion) {
        throw new KeeperException.BadVersionException();
      }
      this.data = newData;
      stat.setDataLength((newData == null) ? 0 : newData.length);
      stat.setVersion(version.addAndGet(1));
      signalNodeEvent(EventType.NodeDataChanged);
    }

    public List<ACL> getAcl() {
      return Collections.unmodifiableList(acl);
    }

    public Stat getStat() {
      return stat;
    }

    public Map<String, ZNode> getChildren() {
      return Collections.unmodifiableMap(children);
    }

    @Override
    public Iterator<ZNode> iterator() {
      return new ZNodeTreeIterator(this);
    }

    /**
     * Iterates across all ZNodes in the sub-tree rooted at the specified node
     * (will also return the specified ZNode).
     */
    private static class ZNodeTreeIterator implements Iterator<ZNode> {
      private boolean selfReturned = false;
      private ZNode initialZNode;
      private Iterator<ZNode> childIter;
      private Iterator<ZNode> childTreeIter;
      private ZNode currentZNode;

      private ZNodeTreeIterator(ZNode initialZNode) {
        this.initialZNode = initialZNode;
        List<ZNode> childrenCopy =
          new ArrayList<ZNode>(initialZNode.getChildren().values());
        childIter = childrenCopy.iterator();
      }

      @Override
      public boolean hasNext() {
        if (!selfReturned) {
          return true;
        }
        if (childIter.hasNext()) {
          return true;
        }
        if (childTreeIter != null && childTreeIter.hasNext()) {
          return true;
        }
        return false;
      }

      @Override
      public ZNode next() {
        if (!selfReturned) {
          selfReturned = true;
          currentZNode = initialZNode;
          return initialZNode;
        }
        if (childTreeIter == null || !childTreeIter.hasNext()) {
          childTreeIter = childIter.next().iterator();
        }
        currentZNode = childTreeIter.next();
        return currentZNode;
      }

      @Override
      public void remove() {
        try {
          currentZNode.delete(-1);
        } catch (KeeperException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /**
   * Encapsulates a Watcher and the context in which it was created
   */
  private static class ContextedWatcher implements Watcher {
    private final Watcher watcher;
    private final WatchContext watchContext;

    private ContextedWatcher(
      Watcher watcher, long sessionId, WatchTriggerPolicy.WatchType watchType
    ) {
      this.watcher = watcher;
      this.watchContext = new WatchContext(sessionId, watchType);
    }

    public long getSessionId() {
      return watchContext.getSessionId();
    }

    public boolean shouldTrigger(EventType eventType) {
      return watchContext.shouldTrigger(eventType);
    }

    public void merge(ContextedWatcher contextedWatcher) {
      assert(watcher.equals(contextedWatcher.watcher));
      watchContext.merge(contextedWatcher.watchContext);
    }

    @Override
    public void process(WatchedEvent event) {
      watcher.process(event);
    }

    @Override
    public boolean equals(Object o) {
      // Equality is only determined by the watcher
      if (this == o) {
        return true;
      }
      if (!(o instanceof ContextedWatcher)) {
        return false;
      }

      final ContextedWatcher that = (ContextedWatcher) o;

      if (!watcher.equals(that.watcher)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      // Hash code only computed from the watcher
      return watcher.hashCode();
    }

    private static class WatchContext {
      private final Set<WatchTriggerPolicy.WatchType> watchTypeSet =
        EnumSet.noneOf(WatchTriggerPolicy.WatchType.class);
      private long sessionId;

      private WatchContext(long sessionId, WatchTriggerPolicy.WatchType watchType) {
        this.sessionId = sessionId;
        watchTypeSet.add(watchType);
      }

      public long getSessionId() {
        return sessionId;
      }

      public boolean shouldTrigger(EventType eventType) {
        for (WatchTriggerPolicy.WatchType watchType : watchTypeSet) {
          if (WatchTriggerPolicy.shouldTrigger(watchType, eventType)) {
            return true;
          }
        }
        return false;
      }

      public void merge(WatchContext watchContext) {
        assert(sessionId == watchContext.getSessionId());
        watchTypeSet.addAll(watchContext.watchTypeSet);
      }
    }
  }

  /**
   * Defines the ZooKeeper policies for when a particular watch type should be
   * triggered.
   */
  private static class WatchTriggerPolicy {
    private enum WatchType {
      EXISTS,
      GETDATA,
      GETCHILDREN;
    }

    private static Map<WatchType, Set<EventType>> mapping = constructMapping();

    private static Map<WatchType, Set<EventType>> constructMapping() {
      Map<WatchType, Set<EventType>> mapping =
        new EnumMap<WatchType, Set<EventType>>(WatchType.class);
      mapping.put(WatchType.EXISTS,
        EnumSet.of(
          EventType.NodeCreated,
          EventType.NodeDeleted,
          EventType.NodeDataChanged
        )
      );
      mapping.put(WatchType.GETDATA,
        EnumSet.of(
          EventType.NodeDeleted,
          EventType.NodeDataChanged
        )
      );
      mapping.put(WatchType.GETCHILDREN,
        EnumSet.of(
          EventType.NodeChildrenChanged,
          EventType.NodeDeleted
        )
      );
      return mapping;
    }

    public static boolean shouldTrigger(WatchType watchType, EventType eventType) {
      return mapping.get(watchType).contains(eventType);
    }
  }
}
