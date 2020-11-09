package com.facebook.logging;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 30, time = 1, timeUnit = TimeUnit.SECONDS)
public class LoggerBenchmark {

  Logger logger;

  @Setup
  public void init() {
    logger = LoggerImpl.getClassLogger();
  }

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  public void benchmarkWithNoArg() {
    logger.warn("test");
  }

  @Benchmark
  public void benchmarkWithOneArg() {
    logger.warn("test with %s", "arg1");
  }

  @Benchmark
  public void benchmarkWithTwoArgs() {
    logger.warn("test with %s, %s", "arg1", "arg2");
  }

  @Benchmark
  public void benchmarkWithThreeArgs() {
    logger.warn("test with %s, %s, %s", "arg1", "arg2", "arg3");
  }
}
