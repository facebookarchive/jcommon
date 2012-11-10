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
package com.facebook.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Reads and expands a JSON config object through a series of includes.
 * Expects JSON config objects to consist of two top level keys: 'conf' and 'includes':
 *
 * conf - key-value pairs to be consolidated in the returned JSONObject [req]
 * includes - list of JSON configs (of the same format) from which to pull and
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
 *    object1,
 *    object2
 *   ]
 * }
 *
 * Keys in the existing or closer config objects will take precedence over the same
 * key defined in more distantly included objects.
 */
public abstract class AbstractExpandedConfJSONProvider implements JSONProvider {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractExpandedConfJSONProvider.class);
  private static final String CONF_KEY = "conf";
  private static final String INCLUDES_KEY = "includes";

  private final String root;

  public AbstractExpandedConfJSONProvider(String root) {
    this.root = root;
  }

  private JSONObject getExpandedJSONConfig() throws JSONException {
    Set<String> traversedFiles = new HashSet<>();
    Queue<String> toTraverse = new LinkedList<>();

    // Seed the graph traversal with the root node
    traversedFiles.add(root);
    toTraverse.add(root);

    // Policy: parent configs will override children (included) configs
    JSONObject expanded = new JSONObject();
    while (!toTraverse.isEmpty()) {
      String current = toTraverse.remove();
      JSONObject json = load(current);
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
          String include = resolve(current, includes.getString(idx));
          if (traversedFiles.contains(include)) {
            LOG.warn("Config file was included twice: " + include);
          } else {
            toTraverse.add(include);
            traversedFiles.add(include);
          }
        }
      }
    }

    return expanded;
  }

  @Override
  public JSONObject get() throws JSONException {
    return getExpandedJSONConfig();
  }

  /**
   * Determines the canonical resource identifier for the given <tt>config</tt>.
   * This hook allows subclasses to resolve relative paths used in includes.
   *
   * @param parent the resource which listed this config in its includes
   *               (or <tt>null</tt> if it is the root)
   * @param config a config resource identifier
   * @return the canonical resource identifier
   * @see com.facebook.config.ExpandedConfFileJSONProvider#resolve(String, String)
   */
  abstract protected String resolve(String parent, String config);

  /**
   * Returns the configuration JSON identified by <tt>config</tt>.
   * <tt>config</tt> is guaranteed to be the result of some call to
   * {@link #resolve(String, String)}.
   *
   * @param config a canonical resource identifier
   * @return a JSON representation of the resource's contents
   * @see com.facebook.config.ExpandedConfFileJSONProvider#load(String)
   */
  abstract protected JSONObject load(String config) throws JSONException;
}
