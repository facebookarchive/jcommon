package com.facebook.zookeeper.convenience;

import com.facebook.zookeeper.ZooKeeperIface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * This abstract base class provides a template-and-hook style for writing
 * scripts interacting with ZooKeeper. Provided that subclasses implement
 * the abstracted methods, this class will handle initiating/terminating the
 * ZooKeeper connection as well as parsing relevant command line parameters.
 * The script may be executed via the runMain(...) method.
 */
public abstract class ZkScript {
  private final ZkQuickConnectionManager zkQuickConnectionManager;

  protected ZkScript(ZkQuickConnectionManager zkQuickConnectionManager) {
    this.zkQuickConnectionManager = zkQuickConnectionManager;
  }

  public void connect(String server, int timeout)
    throws IOException, InterruptedException, TimeoutException {
    zkQuickConnectionManager.connect(server, timeout);
  }

  public void close() throws InterruptedException {
    zkQuickConnectionManager.close();
  }

  protected ZooKeeperIface getZk() {
    return zkQuickConnectionManager.getZk();
  }

  /*
   * Derived scripts must fill in the templated methods below:
   */

  /**
   * @return Name of the derived class
   */
  protected abstract String getName();

  /**
   * @return Command line options specific to the derived script
   */
  protected abstract Options getSpecificOptions();

  /**
   * Verifies the integrity of the script specific command line parameters
   * @param cmd
   * @return true, if verified, false if there is an issue
   */
  protected abstract boolean verifySpecificOptions(CommandLine cmd);

  /**
   * Executes the script (assuming already connected to ZooKeeper)
   * @param cmd
   * @throws Exception
   */
  protected abstract void runScript(CommandLine cmd) throws Exception;

  private Options getOptions() {
    Options options = new Options();
    options.addOption(
      "s",
      "server",
      true,
      "Zookeeper server as host:port string"
    );
    options.addOption(
      "i",
      "tier",
      true,
      "Zookeeper SMC tier name (alternative to server parameter)"
    );
    options.addOption(
      "t",
      "timeout",
      true,
      "Session timeout in milliseconds [Default: 5000]"
    );
    options.addOption(
      "h",
      "help",
      false,
      "Prints this message"
    );
    // Incorporate the specific options from the derived script
    for (Option opt : (Collection<Option>) getSpecificOptions().getOptions()) {
      options.addOption(opt);
    }
    return options;
  }

  private void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(getName(), getOptions());
    System.out.println(
      "Note: You must specify either a specific ZooKeeper server (-s) or an " +
        "SMC ZooKeeper tier (-i)"
    );
  }

  /**
   * Main method to launch any script that derives from this abstract class
   * @param args
   * @throws Exception
   */
  public void runMain(String[] args) throws Exception {
    Options options = getOptions();
    CommandLineParser parser = new GnuParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("help")) {
      printUsage();
      return;
    }

    // Verify script specific options
    if (!verifySpecificOptions(cmd)) {
      printUsage();
      return;
    }

    // Get connection parameters
    String servers;
    if (cmd.hasOption("server")) {
      servers = cmd.getOptionValue("server");
    } else {
      System.err.println(
        "You did not specify a ZooKeeper server or an SMC ZooKeeper tier!"
      );
      printUsage();
      return;
    }
    int timeout = Integer.parseInt(cmd.getOptionValue("timeout", "5000"));

    // Run the script
    connect(servers, timeout);
    try {
      runScript(cmd);
    } finally {
      close();
    }
  }
}
