## What is it?
`jcommon-concurrent-stats` is an alternative stat implementation optimized for highly-concurrent
updates. It trades increased memory usage for lower contention than the default implementation
provided by `jcommon-stats`.

The `Stats` class in `jcommon-stats` provides methods for creating both kinds:
- `Stats.getRate("my-stat")`/`Stats.concurrentRate("my-concurrent-stat")`
- `Stats.getSpread("my-stat")`/`Stats.concurrentSpread("my-concurrent-stat")`
- `Stats.concurrentSpreadRate("my-concurrent-stat")` (cheaper than creating them separately)

See the `RateStat` and `SpreadStat` JavaDocs for more details on their implementation and accuracy.

## What does it depend on?

Nothing but built-in Java classes.

Care was taken to avoid introducing any dependencies, so you can use these libraries with any
existing code base without worry of dependency conflicts. (So if you submit any pull requests,
please try not to introduce any new dependencies.)

This is the only reason for a separate module. Otherwise, these classes would be part of
`jcommon-stats`.
