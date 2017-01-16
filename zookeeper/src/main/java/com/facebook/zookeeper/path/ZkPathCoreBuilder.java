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
package com.facebook.zookeeper.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZkPathCoreBuilder {
  private final String appRoot;
  private final List<String> zNodes = new ArrayList<>();

  public ZkPathCoreBuilder(ZkPathCore basePathCore) {
    this(basePathCore.getAppRoot());
    zNodes.addAll(basePathCore.getZNodes());
  }

  public ZkPathCoreBuilder(String appRoot) {
    this.appRoot = appRoot;
  }

  /**
   * Parses the zNodes from the specified path.
   * Assumes path will be structured as <appRoot>/<zNode1>/<zNode2>/...
   * and that it does not have a terminating slash at the end.
   * @param fullPath
   * @return this builder instance
   */
  public ZkPathCoreBuilder parse(String fullPath) {
    if (fullPath.endsWith("/") && !fullPath.equals("/")) {
      throw new IllegalArgumentException("Invalid path: " + fullPath);
    }
    // Need special handling if appRoot is the ZooKeeper root: "/"
    String normalizedAppRoot = appRoot.equals("/") ? "" : appRoot;
    zNodes.clear();
    if (fullPath.equals(appRoot)) {
      return this;
    }
    if (!fullPath.startsWith(normalizedAppRoot + "/")) {
      throw new IllegalArgumentException("Could not parse: " + fullPath);
    }
    String stripped = fullPath.replaceFirst(normalizedAppRoot + "/", "");
    zNodes.addAll(Arrays.asList(stripped.split("/")));
    return this;
  }

  /**
   * Append a zNode
   * @param zNode
   * @return this builder instance
   */
  public ZkPathCoreBuilder append(String zNode) {
    zNodes.add(zNode);
    return this;
  }

  /**
   * Remove the last zNode
   * @return this builder instance
   */
  public ZkPathCoreBuilder remove() {
    zNodes.remove(zNodes.size()-1);
    return this;
  }

  /**
   * Removes zNodes until there are targetSize remaining
   * @param targetSize
   * @return this builder instance
   */
  public ZkPathCoreBuilder truncateToSize(int targetSize) {
    assert(targetSize >= 0);
    while (zNodes.size() > targetSize) {
      remove();
    }
    return this;
  }

  public ZkPathCore build() {
    return new ZkPathCore(appRoot, zNodes);
  }
}
