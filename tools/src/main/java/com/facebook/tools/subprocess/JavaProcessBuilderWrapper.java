/*
 * Copyright (C) 2014 Facebook, Inc.
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
package com.facebook.tools.subprocess;

import com.facebook.tools.ErrorMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class JavaProcessBuilderWrapper implements ProcessBuilderWrapper {
  @Override
  public Process createProcess(
    RedirectErrorsTo redirectErrorsTo,
    Map<String, String> environmentOverrides,
    File workingDirectory,
    List<String> command
  ) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);

    if (workingDirectory != null) {
      processBuilder.directory(workingDirectory);
    }

    if (!environmentOverrides.isEmpty()) {
      Map<String, String> environment = processBuilder.environment();

      for (Map.Entry<String, String> entry : environmentOverrides.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (value == null) {
          environment.remove(key);
        } else {
          environment.put(key, value);
        }
      }
    }

    if (redirectErrorsTo == RedirectErrorsTo.STDOUT) {
      processBuilder.redirectErrorStream(true);
    }

    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new ErrorMessage(e, "Error running: %s", command);
    }
  }
}
