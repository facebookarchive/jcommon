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

import java.util.Iterator;

public class ZkGenericPath {
  private final ZkPathCore pathCore;

  public ZkGenericPath(String appRoot) {
    this(new ZkPathCore(appRoot));
  }

  private ZkGenericPath(ZkPathCore pathCore) {
    this.pathCore = pathCore;
  }

  public static ZkGenericPath parse(String appRoot, String fullPath) {
    return new ZkGenericPath(
      new ZkPathCoreBuilder(appRoot)
        .parse(fullPath)
        .build()
    );
  }

  public boolean isRoot() {
    return pathCore.getZNodes().isEmpty();
  }

  public ZkGenericPath getParent() {
    if (pathCore.getZNodes().isEmpty()) {
      throw new IllegalStateException("No more parents");
    }
    return new ZkGenericPath(
      new ZkPathCoreBuilder(pathCore)
        .remove()
        .build()
    );
  }

  public ZkGenericPath appendChild(String child) {
    return new ZkGenericPath(
      new ZkPathCoreBuilder(pathCore)
        .append(child)
        .build()
    );
  }

  public Iterator<ZkGenericPath> lineageIterator() {
    return new LineageIterator();
  }

  @Override
  public String toString() {
    return pathCore.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZkGenericPath)) {
      return false;
    }

    final ZkGenericPath that = (ZkGenericPath) o;

    if (!pathCore.equals(that.pathCore)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return pathCore.hashCode();
  }

  private class LineageIterator implements Iterator<ZkGenericPath> {
    private final Iterator<String> nodeIter = pathCore.getZNodes().iterator();
    private boolean appRootReturned = false;
    private ZkGenericPath previousPath;

    @Override
    public boolean hasNext() {
      return !appRootReturned || nodeIter.hasNext();
    }

    @Override
    public ZkGenericPath next() {
      if (!appRootReturned) {
        appRootReturned = true;
        previousPath = new ZkGenericPath(pathCore.getAppRoot());
        return previousPath;
      }
      previousPath = previousPath.appendChild(nodeIter.next());
      return previousPath;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
