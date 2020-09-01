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
package com.facebook.util;

import com.google.common.io.PatternFilenameFilter;
import java.io.File;
import java.io.FilenameFilter;

public class FileUtils {
  private FileUtils() {
    throw new AssertionError("not instantiable");
  }

  public static String backtrackToFile(String startDir, String targetFile) {
    FilenameFilter filenameFilter = new PatternFilenameFilter(targetFile);
    File currentDir = new File(startDir).getAbsoluteFile();

    while (currentDir.getParentFile() != null) {
      String[] files = currentDir.list(filenameFilter);
      if (files.length == 1) {
        return currentDir.getAbsolutePath();
      } else {
        currentDir = currentDir.getParentFile();
      }
    }

    return null;
  }
}
