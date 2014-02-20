jcommon: a set of libraries used inside Facebook java projects,
internal and open source.  This is the source, and the latest maven
artifact can be found at maven central:

  http://search.maven.org/#search%7Cga%7C1%7Ccom.facebook.jcommon

Documentation:

  https://github.com/facebook/jcommon/wiki/jcommon

jcommon-all is a 'bomb' that includes the whole thing.  You may also
depend on individual pieces such as 'stats' or 'concurrency' if
desired (but this isn't recommended, as you may run into transitive
version issues if you depend on an old stats and new config module
(making this example up)

License is Apache2, see:

  http://www.apache.org/licenses/LICENSE-2.0.html

The short form is in License.txt.  This is *not* present in each file,
but we plan to do so.  In the case you need to redistribute any subset
of jcommon, please include the License.txt and add the contents to the
files.

Thanks!

Committers:
  rash@fb.com
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


