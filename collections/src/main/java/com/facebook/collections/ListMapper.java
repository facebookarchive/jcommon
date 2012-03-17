package com.facebook.collections;

import java.util.ArrayList;
import java.util.List;

public class ListMapper {
  public static <X,Y> List<Y> map(List<X> list, Mapper<X, Y> mapper) {
    List<Y> result = new ArrayList<Y>();

    for (X item : list) {
      result.add(mapper.map(item));
    }
    
    return result;
  }
}
