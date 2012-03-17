package com.facebook.zookeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ZkUtil {
  private static final Charset CHARSET = Charset.forName("UTF-8");

  /**
   * Don't allow construction of this class
   */
  private ZkUtil() {
  }

  /**
   * Converts String into a byte array using a fixed character set. All
   * classes making this conversion should use this function to ensure a
   * consistent data representation in ZooKeeper.
   * @param str
   * @return byte array representation of String
   */
  public static byte[] stringToBytes(String str) {
    return str.getBytes(CHARSET);
  }

  /**
   * Converts a byte array from ZooKeeper into a String.
   * @param data
   * @return String representation of byte array
   */
  public static String bytesToString(byte[] data) {
    return data == null ? null : new String(data, CHARSET);
  }

  /**
   * Filters the given node list by the given prefixes.
   * This method is all-inclusive--if any element in the node list starts
   * with any of the given prefixes, then it is included in the result.
   *
   * @param nodes the nodes to filter
   * @param prefixes the prefixes to include in the result
   * @return list of every element that starts with one of the prefixes
   */
  public static List<String> filterByPrefix(
    List<String> nodes, String... prefixes
  ) {
    List<String> lockChildren = new ArrayList<String>();
    for (String child : nodes){
      for (String prefix : prefixes){
        if (child.startsWith(prefix)){
          lockChildren.add(child);
          break;
        }
      }
    }
    return lockChildren;
  }
}
