package com.facebook.zookeeper.cmd;

import com.facebook.util.StreamImporter;
import com.facebook.zookeeper.ZkUtil;
import com.facebook.zookeeper.path.ZkGenericPath;
import com.facebook.zookeeper.ZooKeeperIface;
import com.facebook.zookeeper.convenience.ZkQuickConnectionManager;
import com.facebook.zookeeper.convenience.ZkScript;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ZNodePruner extends ZkScript {
  public static final String DEFAULT_KEYWORD = "freeze";

  private volatile boolean verbose = false;

  public ZNodePruner(ZkQuickConnectionManager zkQuickConnectionManager) {
    super(zkQuickConnectionManager);
  }

  public ZNodePruner() {
    this(new ZkQuickConnectionManager());
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void prunePersistent(String pathStr, String keyword, File saveTemplate)
    throws IOException, InterruptedException, KeeperException {
    InputStream in = new FileInputStream(saveTemplate);
    try {
      prunePersistent(pathStr, keyword, in);
    } finally {
      in.close();
    }
  }

  public void prunePersistent(String pathStr, String keyword, InputStream in)
    throws IOException, InterruptedException, KeeperException {
    prunePersistent(pathStr, keyword, StreamImporter.importLines(in));
  }

  public void prunePersistent(
    String pathStr, String keyword, List<String> toKeep
  ) throws InterruptedException, KeeperException {
    ZkGenericPath path = new ZkGenericPath(pathStr);
    Set<String> keepSet = expandToKeepSet(toKeep);
    internalPrunePersistent(path, keyword, keepSet);
  }

  private void internalPrunePersistent(
    ZkGenericPath path, String keyword, Set<String> keepSet
  ) throws InterruptedException, KeeperException {
    if (isEphemeral(path.toString())) {
      // We should only be scanning persistent nodes (although we may delete
      // ephemeral nodes in order to remove a parent persistent node)
      return;
    }
    if (keepSet.contains(path.toString())) {
      try {
        ZooKeeperIface zk = getZk();

        byte[] data = zk.getData(path.toString(), false, null);

        if (data != null &&
          keyword.equals(ZkUtil.bytesToString(data))
          ) {
          zk.setData(path.toString(), new byte[0], -1);
        }

        List<String> children = zk.getChildren(path.toString(), null);
        for (String child : children) {
          internalPrunePersistent(path.appendChild(child), keyword, keepSet);
        }
      } catch (KeeperException.NoNodeException e) {
        // If the node disappears while scanning it, then just ignore
      }
    } else {
      deleteSubtree(path, keyword);
    }
  }

  private void deleteSubtree(ZkGenericPath path, String keyword)
    throws InterruptedException, KeeperException {
    ZooKeeperIface zk = getZk();
    try {
      if (!isEphemeral(path.toString())) {
        // Only set freeze on non-ephemerals since ephemerals cant have children
        zk.setData(
          path.toString(),
          ZkUtil.stringToBytes(keyword),
          -1
        );
      }

      while (true) {
        List<String> children = zk.getChildren(path.toString(), null);
        for (String child : children) {
          deleteSubtree(path.appendChild(child), keyword);
        }
        try {
          zk.delete(path.toString(), -1);
          break;
        } catch (KeeperException.NotEmptyException e) {
          // Repeat since children were re-added
        }
      }
    } catch (KeeperException.NoNodeException e) {
      // Ignore since we are trying to delete it
    }
    if (verbose) {
      System.out.println("Deleted ZNode: " + path);
    }
  }

  private boolean isEphemeral(String pathStr)
    throws InterruptedException, KeeperException {
    ZooKeeperIface zk = getZk();
    Stat stat = new Stat();
    zk.getData(pathStr, null, stat);
    return stat.getEphemeralOwner() != 0;
  }

  private Set<String> expandToKeepSet(List<String> toKeep) {
    // Add all listed ZNodes as well as all of their ancestors
    Set<String> keepSet = new HashSet<String>();
    for (String pathStr : toKeep) {
      ZkGenericPath path = ZkGenericPath.parse("/", pathStr);
      Iterator<ZkGenericPath> lineageIter = path.lineageIterator();
      while(lineageIter.hasNext()) {
        keepSet.add(lineageIter.next().toString());
      }
    }
    return keepSet;
  }

  @Override
  protected String getName() {
    return ZNodePruner.class.getName();
  }

  @Override
  protected Options getSpecificOptions() {
    Options options = new Options();
    options.addOption(
      "z",
      "zkpath",
      true,
      "ZooKeeper path to prune (includes all descendants) [Required]"
    );
    options.addOption(
      "k",
      "keyword",
      true,
      "Keyword to write into zNodes that will be removed. This may be used " +
        "to help reduce activity on or underneath this node [Default: freeze]"
    );
    options.addOption(
      "f",
      "file-template",
      true,
      "Path to file containing new-line delimited list of ZNodes to save. " +
        "The list only applies to persistent ZNodes. If this parameter is " +
        "not specified, expects the values to be provided via standard input."
    );
    options.addOption(
      "v",
      "verbose",
      false,
      "Print verbose messages [Default: off]"
    );
    return options;
  }

  @Override
  protected boolean verifySpecificOptions(CommandLine cmd) {
    setVerbose(cmd.hasOption("verbose"));
    if (!cmd.hasOption("zkpath")) {
      System.err.println("Error: You must specify a ZooKeeper path.\n");
      return false;
    }
    if (cmd.hasOption("file-template")) {
      File template = new File(cmd.getOptionValue("file-template"));
      if (!template.exists()) {
        System.err.println("Error: invalid file-template path.\n");
        return false;
      }
    }
    return true;
  }

  @Override
  protected void runScript(CommandLine cmd) throws Exception {
    String root = cmd.getOptionValue("zkpath");
    String keyword = cmd.getOptionValue("keyword", ZNodePruner.DEFAULT_KEYWORD);
    if (cmd.hasOption("file-template")) {
      File saveTemplate = new File(cmd.getOptionValue("file-template"));
      prunePersistent(root, keyword, saveTemplate);
    } else {
      prunePersistent(root, keyword, System.in);
    }
  }

  public static void main(String[] args) throws Exception {
    ZkScript script = new ZNodePruner();
    script.runMain(args);
    System.out.println("DONE");
  }
}
