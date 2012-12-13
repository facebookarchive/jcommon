=Welcome to Jcommon=

Like most maven based java project you can get started with "mvn
compile".  Individual parts can be built separately or as one big
library.

Here's a quick summary of some of the structure:

==collections==

  Libraries around collections. Highly concurrent data structures
  optimized for performance and parallel processing, so some
  interfaces that allow data structures to be backed by primitive
  arrays [] in order to save memory (see SimpleHeap, this is used in
  Conversion Tracking to implement a heap on long pairs and triples
  using a completely flat long[]

  CounterMap and SetMap are useful. ConcurrentCache was particularly
  useful, even with Guava's compute-map up through version 11, but I
  hear version 12 has what we need in it now. I need to examine it for
  my use cases and see if we can switch to Guava

  Many of these were added before we started using Guava,
  also. Caution: sometimes we found guava's impl of similar data
  structures either less memory efficient or slower. Prefer Guava to
  writing your own, but when it comes to existing ones here, compare
  perf/memory and choose the right one.

==concurrency==

  The crux of this library is to minimize the # of threads created in
  the JVM. They are, in fact, costly. Too many threads at once, or
  lots of creation/destruction adds overhead. This is a so-far unique
  set of classes that start to treat some # of threads (say, the # of
  cores) like cpus and allows sharing of those. However, it's
  implemented with the java ExecutorService framework, so anything
  that works with that can use these "virtual executors" and "virtual
  threads".  We've seen huge wins with this.

  Lots of useful classes around Executors. They allow you to use a
  single cached thread pool and have "virtual' Executors (basically
  just another Queue, but with the same semantics as a real Executor
  as far as shutdown(), awaitTermination(), etc. 

  The most interesting classes: ExecutorServiceFront and
  ExecutorServiceFrontBuilder (former known as "ESF") does time-slice
  sharing in the case you have fewer total allowed threads in a base
  pool, but sum of layers on top add up to more. Example, 12 cpu
  threads in a cpu pool (which is a front on top of a cached thread
  pool with a limit). If you then have two executors on top of this
  one, that each say, ask for 10, then make sure the 12-max one uses
  time-sharing. Depending on your task lengths (usually prefer
  short--manage long-running cron-like tasks differently), a slice of
  even 1s is fine. If you want finer granularity, try 100-500ms and
  vary.

  UnstoppableExecutorServiceCore: several classes use this as the core
  to simulate an executor. While the ESF provides just the queue and
  drainers ("virtual threads"), it needs to be combined with
  UnstoppableExecutorService to make it useful. This descriptively
  named class simply means if you wrap an ExecutorService in it, the
  shutdown() calls will have the same semantics, but the underlying
  Executor can't be stopped. It's useful to wrap an Executor you want
  to share with a foreign library's code to avoid premature
  shutdown. This class is the core of UnstoppableExecutorService and
  UnstoppableScheduledExecutorService, Same as collections: Many of
  these were added before we started using Guava, also. Caution:
  sometimes we found guava's impl of similar data structures either
  less memory efficient or slower. Prefer Guava to writing your own,
  but when it comes to existing ones here, compare perf/memory and
  choose the right one.  update (2012-06-01): Vanilla Java (aka Huge
  Collections) doesn't solve the memory efficiency issue, but does
  propose interesting ideas of memory mapping the data, and letting
  you define your data structure as an interface

==config==

  Our libraries for parsing and managing JSON config files. Mostly
  convenience, not aware of anything off the top of my head that does
  this, but I'm sure every company has their own if they use JSON

==lifecycle==

  Only one class really here but it's critical in java apps -—a clean
  shutdown means every thread gracefully terminates. This is a class
  now handling that by allowing an extensible number of "stages" to be
  registered and objects indicate the stage they want to be shutdown
  in. Example that drove this: flushing of data from memory had to
  happen before closing streams to HDFS (may sound simple, but there's
  a global close that is done at the end to close a FileSystem, not a
  file).

==logging==

  Facades over log4j that improve performance and add convenience as
  it defers expensive sprintf() format strings. It also includes a
  novel (I haven't seen yet) "time sampling logger" which solves the
  problem of log spew by saying "log at most 1 line per 30 sedonds".


jcommon: a set of libraries used inside Facebook java projects, internal and open source.  This is the source, and the 
latest maven artifact can be found at maven central:

jcommon source code.  You can also find this on maven central.

http://search.maven.org/#search%7Cga%7C1%7Ccom.facebook.jcommon

jcommon-all is a 'bomb' that includes the whole thing.  You may also depend on individual pieces such as
'stats' or 'concurrency' if desired (but this isn't recommended, as you may run into transitive version issues if 
you depend on an old stats and new config module (making this example up)

 
license is Apache2
See : http://www.apache.org/licenses/LICENSE-2.0.html

The short form is in License.txt. At present, this is *not* present in each file.  We plan to do so.  
In the case you need to redistribute any subset of jcommon, please include the License.txt and add the contents to 
the files.

Thanks!

Committers:
sr@fb.com
hgschmie@fb.com
ehwang@fb.com
lif@fb.com
timw@fb.com
martint@fb.com
dain@fb.com
andrewcox@fb.com

Other Contributors:
ajaiswal@fb.com
alessandro@fb.com
groys@fb.com
jasonj@fb.com
junli@fb.com
wwang@fb.com