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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Controls a running operating system command.
 *
 * @see SubprocessBuilder
 */
public interface Subprocess extends Iterable<String>, AutoCloseable {
  /**
   * Returns the operating system command this subprocess is controlling, along with its arguments.
   *
   * @return the command and its arguments
   */
  public List<String> command();

  /**
   * Blocks until the underlying command has terminated.  If the command is in streaming mode,
   * this method puts it in non-streaming mode to ensure it will eventually exit.
   *
   * @return the command's return code
   */
  public int waitFor();

  /**
   * Blocks until the underlying command has terminated, copying stdout to the given stream.
   *
   * @param out the stream to copy to
   * @return the command's return code
   * @throws IllegalStateException if this command was not started in streaming mode
   */
  public int waitFor(OutputStream out);

  /**
   * Blocks until the underlying command has terminated, copying stdout to the given file.
   *
   * @param out the file to copy to
   * @return the command's return code
   * @throws IllegalStateException if this command was not started in streaming mode
   */
  public int waitFor(File outFile);

  /**
   * Immediately destroys the underlying system command.
   */
  public void kill();

  /**
   * Returns the command's return code.  Waits for the command to complete if it hasn't already.
   *
   * @return the comman's return code
   */
  public int returnCode();

  /**
   * Whether the return code was zero.  Waits for the command to complete if it hasn't already.
   *
   * @return true if the return code was zero, otherwise false
   */
  public boolean succeeded();

  /**
   * Whether the return code was non-zero.  Waits for the command to complete if it hasn't already.
   *
   * @return true if the return code was non-zero, otherwise false
   */
  public boolean failed();

  /**
   * Returns the command's stderr output.  If the command writes a lot of data to stderr, this
   * method may truncate some of the output (e.g., it may only return the first 500k).
   *
   * @return the command's stderr output
   */
  public String getError();

  /**
   * Returns the command's stdout output.  If the command writes a lot of data to stdout, this
   * method may truncate some of the output (e.g., it may only return the first 500k).
   *
   * @return the command's stdout output
   */
  public String getOutput();

  /**
   * Returns a stream for reading the command's stdout.
   *
   * @return
   * @throws IllegalStateException if this command was not started in streaming mode
   */
  public BufferedInputStream getStream();

  /**
   * Returns a reader for reading the command's stdout.
   *
   * @return
   * @throws IllegalStateException if this command was not started in streaming mode
   */
  public BufferedReader getReader();

  /**
   * Puts the underlying command in non-streaming mode.  If the command is already in non-streaming
   * mode, has no effect.
   */
  public void background();

  /**
   * Writes data to the command's stdin
   *
   * @param content the data to write
   */
  public void send(String content);

  /**
   * Returns an iterator over each line written by the command to stdout.
   *
   * @return
   * @throws IllegalStateException if this command was not started in streaming mode
   */
  @Override
  Iterator<String> iterator();

  @Override
  void close();
}
