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
package com.facebook.tools.io;

import com.facebook.tools.subprocess.SubprocessBuilder;

import java.io.Console;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container for standard input/output-related objects. Mimics {@link java.lang.System} with the
 * following public fields:
 * <dl>
 * <dt>{@link #out}</dt>
 * <dd>A {@link com.facebook.tools.io.StatusPrintStream} instance for stdout-like output</dd>
 * <dt>{@link #err}</dt>
 * <dd>A {@link com.facebook.tools.io.PrintStreamPlus} instance for stderr-like output</dd>
 * <dt>{@link #in}</dt>
 * <dd>A {@link com.facebook.tools.io.Input} instance for stdin-like input</dd>
 * </dl>
 * <p/>
 * The {@link #ask(Enum, String, Object...)} and {@link #ask(Enum, String)} methods can be used to
 * easily prompt for decisions.
 * <p/>
 * Also included is a {@link #subprocess} builder for spawing new processes.
 * <p/>
 * Some behavior depends on whether Java is being run on an interactive terminal or spawned from
 * another process, e.g., {@literal java -jar my-tool.jar} vs {@literal java -jar my-tool.jar | wc}.
 * If interactive, output to {@link #err} inserts
 * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSII escape codes</a> to render
 * error output as white text on a bright red background. Additional escape codes are used so that
 * consecutive {@link com.facebook.tools.io.Status} methods overwrite previous ones, making them
 * appropriate for outputting status that would otherwise be too spammy, e.g.,
 * {@code io.out.statusf("Finished %s of %s", done, total);}.
 * If non-interactive, no escape codes are inserted, and {@link com.facebook.tools.io.Status}
 * methods do nothing.
 */
public class IO {
  private static final String WHITE_ON_RED = "\033[1;37;41m";
  private static final String DEFAULT_COLORS = "\033[0m";

  public final StatusPrintStream out;
  public final PrintStreamPlus err;
  public final Input in;
  public final SubprocessBuilder subprocess;

  public IO(PrintStream out, PrintStream err, Input in, SubprocessBuilder subprocess) {
    Console console = System.console();

    if (console == null) {
      this.out = new NoninteractiveStatusPrintStream(out);
      this.err = new NoninteractiveStatusPrintStream(err);
    } else {
      ConsoleStatus status = new ConsoleStatus(console.writer());

      this.out = new InteractiveStatusPrintStream(out, status, DEFAULT_COLORS);
      this.err = new InteractiveStatusPrintStream(err, status, WHITE_ON_RED);
      Runtime.getRuntime().addShutdownHook(
        new Thread(
          new Runnable() {
            @Override
            public void run() {
              // reset console color and erase final status line
              IO.this.out.print("");
            }
          }
        )
      );
    }

    this.in = in;
    this.subprocess = subprocess;
  }

  /**
   * Creates a new container using {@link java.lang.System#out}, {@link java.lang.System#err},
   * and {@link java.lang.System#in}.
   */
  public IO() {
    this(System.out, System.err, new InputStreamInput(System.in));
  }

  public IO(PrintStream out, PrintStream err, Input in) {
    this(out, err, in, new SubprocessBuilder());
  }

  /**
   * Convenience method equivalent to <code>ask(defaultValue, String.format(format, args))</code>.
   */
  public <T extends Enum> T ask(T defaultValue, String format, Object... args) {
    return ask(defaultValue, String.format(format, args));
  }

  /**
   * Prompts the user to make a decision. For example:
   * <pre><code>
   * if (io.ask(YesNo.YES, "Are you sure you want to dance").isYes()) {
   *   io.out.println("Dance party!!");
   * } else {
   *   io.out.println("Some other time, then…");
   * }
   * <p/>
   * </code></pre>
   * <pre><tt>
   * > Are you sure you want to dance? [Y/n] x
   * > Are you sure you want to dance? [Y/n] this is not a valid response
   * > Are you sure you want to dance? [Y/n] y
   * > Dance party!!
   * </tt></pre>
   * The options shown are determined by {@code defaultValue} which must be an {@code enum} with
   * its members annotated with {@link com.facebook.tools.io.Answer}. The first
   * {@link com.facebook.tools.io.Answer#value()} is show in the brackets after the prompt, but any
   * of the values is accepted. The values must all be lower-case; the default value, i.e., the one
   * used if the user simply hits enter, is capitalized.
   *
   * @param defaultValue value to use if no input is given
   * @param prompt       message to show user
   * @return the {@code enum} value corresponding to the user's input
   */
  public <T extends Enum> T ask(T defaultValue, String prompt) {
    Class<T> enumClass = defaultValue.getDeclaringClass();
    Field[] fields = enumClass.getFields();
    Map<String, T> answers = new LinkedHashMap<>();
    StringBuilder displayValues = new StringBuilder(16);

    for (Field field : fields) {
      Answer annotation = field.getAnnotation(Answer.class);

      if (annotation != null) {
        int modifiers = field.getModifiers();

        if (enumClass.isAssignableFrom(field.getType()) &&
          Modifier.isStatic(modifiers) &&
          Modifier.isPublic(modifiers)) {
          String fieldName = enumClass.getName() + "." + field.getName();
          T answer;

          try {
            answer = (T) field.get(enumClass);
          } catch (IllegalAccessException | RuntimeException e) {
            throw new IllegalArgumentException("Error accessing " + fieldName, e);
          }

          String[] values = annotation.value();

          if (values == null || values.length == 0) {
            throw new NullPointerException("Missing values for " + fieldName);
          }

          for (String value : values) {
            if (value == null) {
              throw new NullPointerException("Null value for " + fieldName);
            }

            if (!value.equals(value.trim().toLowerCase())) {
              throw new IllegalArgumentException(
                String.format("Values must be all lower-case, but got %s for %s", value, fieldName)
              );
            }

            T existing = answers.put(value, answer);

            if (existing != null) {
              throw new IllegalArgumentException(
                String.format("Duplicate value %s for %s", value, fieldName)
              );
            }
          }

          if (displayValues.length() > 0) {
            displayValues.append("/");
          }

          if (defaultValue == answer) {
            displayValues.append(values[0].toUpperCase());
          } else {
            displayValues.append(values[0]);
          }
        }
      }
    }

    if (answers.isEmpty()) {
      throw new IllegalArgumentException("No fields annotated with @PromptAnswer in " + enumClass);
    }

    String fullPrompt = String.format("%s [%s]? ", prompt, displayValues);
    T answer;

    do {
      out.print(fullPrompt);
      out.flush();

      String response = in.readLine();

      response = response == null ? "" : response.trim().toLowerCase();

      if (response.isEmpty()) {
        answer = defaultValue;
      } else {
        answer = answers.get(response);
      }
    } while (answer == null);

    return answer;
  }
}
