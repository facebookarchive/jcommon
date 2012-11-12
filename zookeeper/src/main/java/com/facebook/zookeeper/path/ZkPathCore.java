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

import java.util.Collections;
import java.util.List;

/**
 * Immutable class representing a ZooKeeper application path
 */
public class ZkPathCore {
  private final String appRoot;
  private final List<String> zNodes;
  private final String pathStr;

  // Package-private constructor
  ZkPathCore(String appRoot, List<String> zNodes) {
    this.appRoot = appRoot;
    this.zNodes = zNodes;
    pathStr = generatePathStr();
  }

  public ZkPathCore(String appRoot) {
    this(appRoot, Collections.<String>emptyList());
  }

  public ZkPathCore(ZkPathCore pathCore) {
    this(pathCore.appRoot, pathCore.zNodes);
  }

  public String getAppRoot() {
    return appRoot;
  }

  public List<String> getZNodes() {
    return Collections.unmodifiableList(zNodes);
  }

  @Override
  public String toString() {
    return pathStr;
  }

  private String generatePathStr() {
    if (zNodes.isEmpty()) {
      return appRoot;
    } else {
      StringBuilder sb = new StringBuilder(computeStringLen());
      // Need special handling if appRoot is the ZooKeeper root: "/"
      String normalizedAppRoot = appRoot.equals("/") ? "" : appRoot;
      sb.append(normalizedAppRoot);
      for (String zNode : zNodes) {
        sb.append("/").append(zNode);
      }
      return sb.toString();
    }
  }

  private int computeStringLen() {
    if (zNodes.isEmpty()) {
      return appRoot.length();
    } else {
      // Need special handling if appRoot is the ZooKeeper root: "/"
      String normalizedAppRoot = appRoot.equals("/") ? "" : appRoot;
      int length = normalizedAppRoot.length();
      for (String zNode : zNodes) {
        length += zNode.length() + 1; // Add one for the slash
      }
      return length;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZkPathCore)) {
      return false;
    }

    final ZkPathCore that = (ZkPathCore) o;

    if (!appRoot.equals(that.appRoot)) {
      return false;
    }
    if (!zNodes.equals(that.zNodes)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = appRoot.hashCode();
    result = 31 * result + zNodes.hashCode();
    return result;
  }
}
