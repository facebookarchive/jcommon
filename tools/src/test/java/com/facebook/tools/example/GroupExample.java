package com.facebook.tools.example;

import com.facebook.tools.CommandDispatcher;
import com.facebook.tools.CommandGroup;
import com.facebook.tools.io.IO;

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
public class GroupExample extends CommandDispatcher {
  public GroupExample(IO io) {
    super(
      io,
      new CreateReplayFile(io),
      new CommandGroup(
        io,
        "ops",
        "Commands used by on-call",
        new ExportCheckpoints(io)
      )
    );
  }

  public GroupExample() {
    this(new IO());
  }

  public static void main(String... args) {
    GroupExample runner = new GroupExample();

    System.exit(runner.run(args));
  }
}
