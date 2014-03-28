This module contains a logger-agnostic interface (though, log4j and %-format-inspired) and an implementation that
uses slf4j for the backend. Additional implementations of Logger may be created using any logging framework, or use
slf4j's many bridges.