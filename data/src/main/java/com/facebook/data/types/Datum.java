package com.facebook.data.types;

/**
 * A Datum is meant to be as generic as possible.  It presents both the data
 * itself along with methods to inspect attributes (getType, isNull).  The
 * idea is to cover all primitives and make lists and maps easy, and any
 * arbitrary extensions possible.
 * 
 * The definition of asBoolean() for say, a MapDatum, will be defined by that
 * class.  It may choose to throw an UnssupportedOperationException, or to 
 * return true if the map is not empty.  Read each impl's documentation.
 * 
 * Certainly, if the underlying impl is a BooleanDatum, asBoolean()
 * should return true/false accordingly
 * 
 * Onte note: unless otherwise stated, implementations need NOT be thread safe
 */

public interface Datum extends Comparable<Datum> {

  /**
   * all as*() methods will make a best effort to return the underlying Datum as the requested type
   *
   * ex:
   *
   * <pre>
   *    IntegerDatum fuu = ...;
   *    String bar = fuu.asString(); // no reason this should fail
   *
   *    StringDatum s = ...;
   *    IntegerDatum n = s.asInteger(); // might throw NumberFormatException
   *
   *    Currently, one helper method exists:
   *
   *    {@link DatumType#isLongCompatible(Datum)} which tells you if the type is something such
   *    that asLong() will succeed (obviously includes long, int, short, byte, bool, and some other
   *    special cases)
   *
   *    Users may write their own condition that asserts the valid DatumTypes for their conversion
   *    based on their data (ex: they know all String data is numeric)
   *
   *    one OPTIONAL TODO: add method canBeX() or something that encapsulates logic.
   *
   *    StringDatum can look at the chars and see if they are [0-9] for integers (longs) when
   *    asked canBeIneger()
   *    or canBeDouble() would check for canBeInteger() || (match X\.Y)
   *
   *    or something similar. Possible future work to give users to avoid exceptions if they
   *    don't mind adding precondition statements
   * </pre>
   *
   * @return
   */
  boolean asBoolean();
  byte asByte();
  short asShort();
  int asInteger();
  long asLong();
  float asFloat();
  double asDouble();
  String asString();

  /**
   * what this returns really doesn't matter. It need only be deterministic
   * so that MurmurHash can take it as input and hash. It should probably
   * also be fast to make distinct counting fast
   * 
   * many Datums don't yet implement this
   * @return
   */
  byte[] asBytes();

  /**
   * should be equivalent to
   *
   * <pre>
   *    datum.getType() == DatumType.NULL
   * </pre>
   *
   * kept for historical and conciseness reasons
   *
   * @return
   */
  boolean isNull();

  /**
   * see {@link DatumType} for a list of valid values
   * @returns
   */
  DatumType getType();

  /**
   * currently a loose contract that will return whatever "raw" object the Datum is wrapping.
   * This is intuitive with primitives, and probably so with Lists and Maps.  Each implmentation
   * wiil document it's asRaw() as it sees fit
   *
   * @return
   */
  Object asRaw();
}