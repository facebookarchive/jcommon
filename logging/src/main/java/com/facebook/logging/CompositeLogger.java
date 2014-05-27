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
package com.facebook.logging;

import java.util.Collection;

public class CompositeLogger implements Logger {
  private static final Logger LOG = LoggerImpl.getClassLogger();

  private final Collection<Logger> loggers;

  public CompositeLogger(Collection<Logger> loggers) {
    this.loggers = loggers;
  }

  @Override
  public boolean isTraceEnabled() {
    return accumulate(
      new OrAccumulator() {
        @Override
        protected boolean getValue(Logger log) {
          return log.isTraceEnabled();
        }
      }
    );
  }

  @Override
  public boolean isDebugEnabled() {
    return accumulate(
      new OrAccumulator() {
        @Override
        protected boolean getValue(Logger log) {
          return log.isDebugEnabled();
        }
      }
    );
  }

  @Override
  public boolean isInfoEnabled() {
    return accumulate(
      new OrAccumulator() {
        @Override
        protected boolean getValue(Logger log) {
          return log.isInfoEnabled();
        }
      }
    );
  }

  @Override
  public boolean isWarnEnabled() {
    return accumulate(
      new OrAccumulator() {
        @Override
        protected boolean getValue(Logger log) {
          return log.isWarnEnabled();
        }
      }
    );
  }

  @Override
  public boolean isErrorEnabled() {
    return accumulate(
      new OrAccumulator() {
        @Override
        protected boolean getValue(Logger log) {
          return log.isErrorEnabled();
        }
      }
    );
  }

  @Override
  public void trace(final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isTraceEnabled()) {
            log.trace(format, args);
          }
        }
      }
    );
  }

  @Override
  public void trace(final Throwable t, final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isTraceEnabled()) {
            log.trace(t, format, args);
          }
        }
      }
    );
  }

  @Override
  public void debug(final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isDebugEnabled()) {
            log.debug(format, args);
          }
        }
      }
    );
  }

  @Override
  public void debug(final Throwable t, final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isDebugEnabled()) {
            log.debug(t, format, args);
          }
        }
      }
    );
  }

  @Override
  public void debug(final String message, final Throwable throwable) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isDebugEnabled()) {
            log.debug(throwable, message);
          }
        }
      }
    );
  }

  @Override
  public void info(final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isInfoEnabled()) {
            log.info(format, args);
          }
        }
      }
    );
  }

  @Override
  public void info(final Throwable t, final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isInfoEnabled()) {
            log.info(t, format, args);
          }
        }
      }
    );
  }

  @Override
  public void info(final String message, final Throwable throwable) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isInfoEnabled()) {
            log.info(throwable, message);
          }
        }
      }
    );
  }

  @Override
  public void warn(final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isWarnEnabled()) {
            log.warn(format, args);
          }
        }
      }
    );
  }

  @Override
  public void warn(final Throwable t, final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isWarnEnabled()) {
            log.warn(t, format, args);
          }
        }
      }
    );
  }

  @Override
  public void warn(final String message, final Throwable throwable) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isWarnEnabled()) {
            log.warn(throwable, message);
          }
        }
      }
    );
  }

  @Override
  public void error(final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isErrorEnabled()) {
            log.error(format, args);
          }
        }
      }
    );
  }

  @Override
  public void error(final Throwable t, final String format, final Object... args) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isErrorEnabled()) {
            log.error(t, format, args);
          }
        }
      }
    );
  }

  @Override
  public void error(final String message, final Throwable throwable) {
    logAll(
      new LoggerOperation() {
        @Override
        public void execute(Logger log) {
          if (log.isErrorEnabled()) {
            log.error(throwable, message);
          }
        }
      }
    );
  }

  @Override
  public String getName() {
    return loggers.toString();
  }

  private boolean accumulate(Accumulator accumulator) {

    for (Logger log : loggers) {
      accumulator.accumulate(log);
    }

    return accumulator.result();
  }

  private void logAll(LoggerOperation loggerOperation) {
    for (Logger log : loggers) {
      try {
        loggerOperation.execute(log);
      } catch (Exception e) {
        LOG.error(e, "error writing to logger %s", log.getName());
      }
    }
  }

  private interface Accumulator {
    boolean accumulate(Logger log);

    boolean result();
  }

  private interface LoggerOperation {
    void execute(Logger log);
  }

  private static abstract class OrAccumulator implements Accumulator {
    private boolean value = false;

    protected abstract boolean getValue(Logger log);

    @Override
    public boolean accumulate(Logger log) {
      return value |= getValue(log);
    }

    @Override
    public boolean result() {
      return value;
    }
  }
}
