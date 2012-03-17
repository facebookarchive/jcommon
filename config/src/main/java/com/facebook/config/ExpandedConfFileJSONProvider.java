package com.facebook.config;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Reads and expands a JSON file through a series of includes.
 * Expects JSON files to consist of two top level keys: 'conf' and 'includes':
 *
 * conf - key-value pairs to be consolidated in the returned JSONObject [req]
 * includes - list of JSON files (of the same format) from which to pull and
 *    include key-value pairs [opt]
 *
 * JSON config file format:
 * {
 *  conf : {
 *    key1 : value1,
 *    key2 : value2
 *    ...
 *  },
 *  includes : [
 *    file1,
 *    file2
 *   ]
 * }
 *
 * Keys in the existing file or closer files will take precedence over the same
 * key defined in more distantly included files.
 */
public class ExpandedConfFileJSONProvider implements JSONProvider {
  private static final Logger LOG = Logger.getLogger(ExpandedConfFileJSONProvider.class);
  private static final String CONF_KEY = "conf";
  private static final String INCLUDES_KEY = "includes";

  private final File configFile;

  public ExpandedConfFileJSONProvider(File configFile) {
    this.configFile = configFile;
  }

  private JSONObject getExpandedJSONConfig() throws JSONException {
    Set<File> traversedFiles = new HashSet<File>();
    Queue<File> toTraverse = new LinkedList<File>();

    // Seed the graph traversal with the root node
    traversedFiles.add(configFile);
    toTraverse.add(configFile);

    // Policy: parent configs will override children (included) configs
    JSONObject expanded = new JSONObject();
    while (!toTraverse.isEmpty()) {
      File file = toTraverse.remove();
      JSONObject json = fileToJSON(file);
      JSONObject conf = json.getJSONObject(CONF_KEY);
      Iterator<String> iter = conf.keys();
      while (iter.hasNext()) {
        String key = iter.next();
        // Current config will get to insert keys before its include files
        if (!expanded.has(key)) {
          expanded.put(key, conf.get(key));
        }
      }
      // Check if the file itself has any included files
      if (json.has(INCLUDES_KEY)) {
        JSONArray includes = json.getJSONArray(INCLUDES_KEY);
        for (int idx = 0; idx < includes.length(); idx++) {
          File iFile = new File(includes.getString(idx));
          if (!iFile.isAbsolute()) {
            // Provide current file directory as base dir for relative path
            iFile = new File(file.getParent(), includes.getString(idx));
          }
          if (!traversedFiles.contains(iFile)) {
            toTraverse.add(iFile);
            traversedFiles.add(iFile);
          } else {
            LOG.warn("Config file was included twice: " +
              iFile.getAbsolutePath());
          }
        }
      }
    }

    return expanded;
  }

  // protected for unit testing purposes
  protected JSONObject fileToJSON(File file) throws JSONException {
    return new FileJSONProvider(file).get();
  }

  @Override
  public JSONObject get() throws JSONException {
    return getExpandedJSONConfig();
  }
}
