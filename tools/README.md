## What is it?
jcommon-tools is a library for creating simple Java command-line tools.

Inspired by python, it provides utilities for:

* Argument parsing, loosely based on [optparse]/[argparse]
* Process spawning, without the usual multi-threading headaches
* Terminal IO, made easier

[optparse]: http://docs.python.org/2/library/optparse.html
[argparse]: http://docs.python.org/2/library/argparse.html

## What does it depend on?

Nothing but Java 7.

Care was taken to avoid introducing any dependencies, so you can use these libraries with any
existing code base without worry of dependency conflicts. (So if you submit any pull requests,
please try not to introduce any new dependencies.)

## Who should use it?
Writing command-line tools in Java is a bad idea. Java is best for things like servers, i.e.,
long-running processes that have lots of memory and CPU at their disposal. Java is bad at
command-line tools:

* It takes a long time to start up.
* It takes a long time to optimize code at runtime — chances are, your tool will exit before it gets
  optimized.
* It's verbose and not very “scripty” which makes it ill-suited for quick little scripts.
* It tries hard to be platform agnostic, so even simple things like shelling out are difficult

So the first step in writing in a Java command-line tool is to ask yourself:
"Why am I doing this in Java?" If you don't have a good answer, then stop right there.

There are some good answers. The most common one being your tool needs to interact with an existing
Java code base, so you're already paying most of the Java tax already.

## What else should I consider using?

There are several other good command-line parsers out there. If you like annotations and automagic
injection, check out [Airline] or [JCommander]. If you like the define-parse-interrogate approach,
but need pre-Java-7 compatibility or prefer less opinionated / more configurable libraries, see
[Apache Commons CLI]. These are all fine choices; use whatever fits your needs or suits your tastes.

[Airline]: https://github.com/airlift/airline
[JCommander]: http://jcommander.org/
[Apache Commons CLI]: http://commons.apache.org/proper/commons-cli/usage.html

## Overview

The [com.facebook.tools] package contains the [CommandRunner] class. Your commands implement the
[CommandBuilder] interface, which has two methods

* `public CliCommand defineCommand()` to tell the runner what the command-line usage is
* `public void runCommand(CliParser parser)` to execute the command

Since command-line arguments are all strings, `defineCommand()` only concerns itself with what the
valid sets of strings you can pass are. The [CliParser] instance passed to `runCommand()` makes it
easy to convert the strings into other types when it comes time to actually run the command. See the
`com.facebook.tools.parser` section below for more details.

[com.facebook.tools]: (src/main/java/com/facebook/tools)
[CommandRunner]: (src/main/java/com/facebook/tools/CommandRunner.java)
[CommandBuilder]: (src/main/java/com/facebook/tools/CommandBuilder.java)
[CliParser]: (src/main/java/com/facebook/tools/parser/CliParser.java)

## Example Usage

There are a number of examples in the [com.facebook.tools.example] package in the test directory.

[com.facebook.tools.example]: (src/main/java/com/facebook/tools/example)

### com.facebook.tools.parser

The parser package contains all the classes for defining your command-line and extracting values
from it. There are four different types of parameter:

1.  Named Options

    - Usage

          ./my-command --input foo.txt
          ./my-command --input=foo.txt
          ./my-command -i bar.txt

    - Code

          builder.addOption("-i", "--input")
            .withMetavar("file")
            .withDescription("Input to process");

    If there is no `withDefault(value)` then the parameter is required. If you want an optional
    parameter but with no default value shown in the help, use `withDefault(null)`.

2.  Flags

    - Usage

          ./my-command --debug
          ./my-command -d

    - Code

          builder.addFlag("-d", "--debug")
            .withDescription("Enable debug mode");

    Flags have a default value of `"false"`, which is set to `"true"` if the flag is present.

3.  Positional Parameters

    - Usage

          ./my-command foo.txt bar.txt

    - Code

          builder.addParameter("source")
            .withMetavar("file")
            .withDescription("Source file");
          builder.addParameter("destination")
            .withMetavar("file")
            .withDescription("Destination file");

    The name passed to `addParameter()` is used to refer to the parameter when using `CliParser`.

4.  Trailing Parameters

    - Usage

          ./my-command a.txt b.txt c.txt

    - Code

          builder.allowTrailingParameters();

    Trailing parameters are fetched using `CliParser.getTrailing()`.

Arguments are extracted using the `CliParser` passed to `runCommand`:

    String input = parser.get("--input");
    boolean debugEnabled = parser.get("--debug", CliConverter.BOOLEAN);
    String environment = parser.get("--environment", OneOfConverter.oneOf("prod", "test"));
    File outputFile = parser.get(
      "--out",
      new CliConverter<File>() {
        @Override
        public File convert(String value) throws Exception {
          return value == null ? null : new File(value);
        }
      }
    );

See the included example [Converters] for more examples.

[Converters]: (src/test/java/com/facebook/tools/example/Converters.java)

### com.facebook.tools.io

The `io` package replaces `System.out`/`err`/`in` for tools. The `IO` class offers replacements
for each one of these public fields, as well as a `subprocess` field (see
com.facebook.tools.subprocess below), and `ask()` methods that can be used to to easily prompt for
decisions.

Some behavior depends on whether Java is being run on an interactive terminal or spawned from /
piped to another process, e.g., `java -jar my-tool.jar` vs `java -jar my-tool.jar | wc`.

If interactive, output to `io.err` inserts ANSII escape codes to render error output as white text
on a bright red background. Additional escape codes are used so that consecutive `io.status()`/
`io.statusf()` methods overwrite previous ones, making them appropriate for outputting status that
would otherwise be too spammy, e.g.:

    io.out.statusf("Finished %s of %s", done, total);

If non-interactive, no escape codes are inserted, and `io.status()`/`io.statusf()` methods do
nothing.

See [ExportCheckpoints] for an example.

[ExportCheckpoints]: (src/test/java/com/facebook/tools/example/ExportCheckpoints.java)

### com.facebook.tools.subprocess

The `subprocess` package makes calling other programs easier. `SubprocessBuilder` creates
`Subprocess` instances. There are two modes a process can be in:

1. streaming:
   An unlimited amount of output is allowed, but the command may block if it is not consumed.
2. non-streaming:
   The command is guranteed to not block, but the amount of output is limited (e.g., first 10k).

Which is to say, if you want to run a quick command that you expect to produce a fixed amount of
output (e.g., `tail -10 some-file.txt`), then you want to use non-streaming mode.  If you want to
run a command that can produce a lot of data, (e.g., `tail -f some-other-file.txt`) then you want to
use streaming mode.

Streaming commands are started by calling `subprocessBuilder.stream()`.
Non-streaming commands are started by calling `subprocessBuilder.start()`.
A streaming command can be turned into a non-streaming command by calling `subprocess.background()`.

Stderr is always in non-streaming mode.  This ensures you never have to worry about your command
blocking because you haven't read error output.  If you expect (and need to process) a lot of output
to stderr, then you need to call `subprocessBuilder.redirectStderrToStdout()`.

See [CompareFiles] for an example.

[CompareFiles]: (src/test/java/com/facebook/tools/example/CompareFiles.java)

## Really-Executable Tools

A major usability issue with writing a Java command-line program is training your customers to add a
`java -jar` in front of every invocation. Turns out, there is a way to make a jar file executable,
allowing you to run it just like any other program, e.g., `java -jar mytool.jar` becomes
`./mytool.jar`. Or you can rename `mytool.jar` to `mytool` and no one needs to know the shameful
secret that it's actually a jar.

The suprising details are described in an excellent [blog post] by the excellent
[Brian McCallister], who has also gone to the trouble of creating a [maven plugin] to do this as
part of the build process. Check it out, it's excellent.

[blog post]: http://skife.org/java/unix/2011/06/20/really_executable_jars.html
[Brian McCallister]: http://skife.org/about.html
[maven plugin]: https://github.com/brianm/really-executable-jars-maven-plugin
