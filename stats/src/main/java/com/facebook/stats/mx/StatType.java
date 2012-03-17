package com.facebook.stats.mx;

/**
 * String wrapper that can memoize and efficiently do prepend/append/INSERT
 *
 * For use with counters that need a default and a dynamic one (ex: per-
 * category)
 */
public interface StatType {
  /**
   * 
   * @return the current key
   */
  public String getKey();

  /**
   * 
   * @param suffix - append ".suffix"
   * @return returns getKey() + suffix
   */
  public StatType append(String suffix);

  /**
   * 
   * @param prefix - prepend "prefix."
   * @return returns prefix +  getKey()
   */
  public StatType prepend(String prefix);

  /**
   * will create StatType such that
   * 
   * getKey() = prefix + value + suffix
   * 
   * but leaves the internal prefix + suffix the same.  ex:
   * 
   * materialize("x").materialize("y").getKey() = prefix + "y" + suffix
   * 
   * @param value - string to use as replacement value
   * @return new StatType on which getKey() returns as specified
   */
  public StatType materialize(String value);
}
