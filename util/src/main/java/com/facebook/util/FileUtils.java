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

    while (currentDir.getParentFile() != null){
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
