package com.facebook.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamImporter {
  public static List<String> importLines(InputStream in) throws IOException {
    List<String> lines = new ArrayList<String>();
    BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = buffered.readLine()) != null)   {
      lines.add(line);
    }
    return lines;
  }
}
