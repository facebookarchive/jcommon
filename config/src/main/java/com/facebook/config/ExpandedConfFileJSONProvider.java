package com.facebook.config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ExpandedConfFileJSONProvider extends AbstractExpandedConfJSONProvider {
  public ExpandedConfFileJSONProvider(File root) {
    super(root.getAbsolutePath());
  }

  @Override
  protected String resolve(String parent, String config) {
    File file = new File(config);
    if (file.isAbsolute() || parent == null) {
      return file.getAbsolutePath();
    }

    // relative path
    File parentFile = new File(parent);
    file = new File(parentFile.getParent(), config);

    return file.getAbsolutePath();
  }

  @Override
  protected JSONObject load(String config) throws JSONException {
    File configFile = new File(config);

    return new FileJSONProvider(configFile).get();
  }
}
