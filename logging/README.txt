This package serves as a backwards compatible package for all components
that depends on version 0.1.8 and before of jcommon. It is fully
backwards-compatible with previous version in that it also pulls in log4j as a dependency,
but usage is strongly discouraged.
In general, libraries should avoid depending on a particular logging framework.

This module is scheduled to go away by the end of 2013.

The following packages have been introduced as replacements for the set
of use cases this package tried to address:

1. As a log4j wrapper that supports varargs: use SLF4J directly
2. Use of LoggerImpl.getClassLogger(): use LoggerUtil in logging-util instead
3. Use of TimeSamplingLogger: use TimeSamplingSLF4JLogger in logging-util instead