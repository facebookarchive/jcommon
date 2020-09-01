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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

/** Provides JSON stored in a file */
public class FileJSONProvider implements JSONProvider {
  private static final Pattern COMMENT_LINE = Pattern.compile("^\\s*//.*");

  private final File file;

  public FileJSONProvider(File file) {
    this.file = file;
  }

  protected BufferedReader getReader() throws FileNotFoundException {
    return new BufferedReader(new FileReader(file));
  }

  private JSONObject fileToJSON() throws IOException, JSONException {
    StringBuilder sb = new StringBuilder(1024);
    BufferedReader in = null;

    try {
      in = getReader();
      String line;

      while ((line = in.readLine()) != null) {
        // skip simple comment lines
        if (!COMMENT_LINE.matcher(line).find()) {
          sb.append(line);
        }
      }

    } finally {
      if (in != null) {
        in.close();
      }
    }

    return new JSONObject(sb.toString());
  }

  @Override
  public JSONObject get() throws JSONException {
    try {
      return fileToJSON();
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }
}
