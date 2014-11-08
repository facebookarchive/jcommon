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
package com.facebook.tools.example;

import com.facebook.tools.CommandBuilder;
import com.facebook.tools.CommandRunner;
import com.facebook.tools.io.IO;
import com.facebook.tools.io.YesNo;
import com.facebook.tools.parser.CliCommand;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.parser.OneOfConverter;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;

import java.util.Map;

import static com.google.common.collect.Iterables.concat;

public class ExportCheckpoints implements CommandBuilder {
  private final IO io;

  public ExportCheckpoints(IO io) {
    this.io = io;
  }

  @Override
  public CliCommand defineCommand() {
    CliCommand.Builder builder = new CliCommand.Builder(
      "export-checkpoints", "Export checkpoints, with optional deletion."
    );

    builder.addOption("-h", "--host")
      .withMetavar("host:port")
      .withDescription("Address of checkpoint manager service");
    builder.addOption("-a", "--app", "--application")
      .withMetavar("application")
      .withDescription("Application name");
    builder.addOption("-e", "--env", "--environment")
      .withMetavar("environment")
      .withDescription("Environment name")
      .withExample("prod", "staging")
      .withDefault("prod");
    builder.addOption("--shards")
      .withMetavar("list")
      .withDescription("List of shards to export")
      .withExample("0,1,2,5-12,19")
      .allowMultiple()
      .withDefault(null);
    builder.addFlag("--delete")
      .withDescription("Whether to delete the checkpoints.");
    ThriftService.mixin(builder);

    return builder.build();
  }

  @Override
  public void runCommand(CliParser parser) {
    String application = parser.get("--application");
    String environment = parser.get("--environment", OneOfConverter.oneOf("prod", "test"));
    Iterable<Integer> shards = concat(parser.getMulti("--microshards", Converters.INT_LIST));
    boolean delete = parser.get("--delete", Converters.BOOLEAN);
    HostAndPort host = parser.get("--host", Converters.HOST_PORT);
    ThriftService<CheckpointManager> managerService =
      new ThriftService<>(CheckpointManager.class, parser);

    try (CheckpointManager manager = managerService.openService(host)) {
      Map<Integer, String> checkpoints = listCheckpoints(manager, application, environment, shards);

      if (delete) {
        if (checkpoints.isEmpty()) {
          io.out.println("No checkpoints found to delete.");
        } else {
          YesNo proceed = io.ask(
            YesNo.NO,
            "WARNING: Delete %,d checkpoints for %s %s (this operation is NOT reversible)",
            checkpoints.size(),
            application,
            environment
          );

          if (proceed.isYes()) {
            deleteCheckpoints(manager, application, environment, checkpoints.keySet());
          } else {
            io.out.println("Delete cancelled.");
          }
        }
      }
    }
  }

  public Map<Integer, String> listCheckpoints(
    CheckpointManager manager, String application, String environment, Iterable<Integer> shards
  ) {
    ImmutableMap.Builder<Integer, String> result = ImmutableMap.builder();

    for (int shard : shards) {
      String checkpoint = manager.getCheckpoint(application, environment, shard);

      if (checkpoint == null) {
        io.out.statusf("Skipping null checkpoint %s", shard);
      } else {
        result.put(shard, checkpoint);
        io.out.printfln("%s: %s", shard, checkpoint);
      }
    }

    io.out.clearStatus();

    return result.build();
  }

  public void deleteCheckpoints(
    CheckpointManager manager, String application, String environment, Iterable<Integer> shards
  ) {
    for (int shard : shards) {
      io.out.statusf("Deleting checkpoint %s", shard);
      manager.deleteCheckpoint(application, environment, shard);
    }

    io.out.clearStatus();
  }

  public static void main(String... args) {
    IO io = new IO();

    CommandRunner runner = new CommandRunner(io, new ExportCheckpoints(io));

    System.exit(runner.run(args));
  }
}
