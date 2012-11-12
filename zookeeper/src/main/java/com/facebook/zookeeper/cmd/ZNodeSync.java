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
package com.facebook.zookeeper.cmd;

import com.facebook.util.StreamImporter;
import com.facebook.zookeeper.convenience.ZkQuickConnectionManager;
import com.facebook.zookeeper.convenience.ZkScript;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ZNodeSync extends ZkScript {
  private final ZNodeBulkLoader loader;
  private final ZNodePruner pruner;

  public ZNodeSync(ZkQuickConnectionManager zkQuickConnectionManager) {
    super(zkQuickConnectionManager);
    loader = new ZNodeBulkLoader(zkQuickConnectionManager);
    pruner = new ZNodePruner(zkQuickConnectionManager);
    // Share the provided connection
  }

  public ZNodeSync() {
    this(new ZkQuickConnectionManager());
  }

  public void setVerbose(boolean verbose) {
    loader.setVerbose(verbose);
    pruner.setVerbose(verbose);
  }

  @Override
  protected String getName() {
    return ZNodeSync.class.getName();
  }

  @Override
  protected Options getSpecificOptions() {
    Options options = new Options();
    options.addOption(
      "z",
      "zkpath",
      true,
      "ZooKeeper path to sync (includes all descendants) [Required]"
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
      "Path to file containing new-line delimited list of ZNodes to sync. " +
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
    setVerbose(cmd.hasOption("verbose"));
    String root = cmd.getOptionValue("zkpath");
    String keyword = cmd.getOptionValue("keyword", ZNodePruner.DEFAULT_KEYWORD);
    InputStream in =
      (cmd.hasOption("file-template"))
        ? new FileInputStream(new File(cmd.getOptionValue("file-template")))
        : System.in;
    try {
      List<String> template = StreamImporter.importLines(in);
      // First run the bulk loader
      loader.load(template);
      // Then prune everything else under specified root
      pruner.prunePersistent(root, keyword, template);
    } finally {
      in.close();
    }
  }

  public static void main(String[] args) throws Exception {
    ZkScript script = new ZNodeSync();
    script.runMain(args);
    System.out.println("DONE");
  }
}
