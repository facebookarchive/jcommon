package com.facebook.stats.cardinality;

import com.google.common.base.Throwables;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class UnsafeUtil {
  private static final Unsafe unsafe;

  static {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      unsafe = (Unsafe) theUnsafe.get(null);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public static int sizeOf(Class<?> clazz) {
    long maxOffset = -1;
    Field lastField = null;
    while (clazz != null) {
      for (Field field : clazz.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          long offset = unsafe.objectFieldOffset(field);
          if (offset > maxOffset) {
            lastField = field;
            maxOffset = offset;
          }
        }
      }
      clazz = clazz.getSuperclass();
    }

    if (lastField != null) {
      Class<?> fieldType = lastField.getType();
      Class<?> arrayType;
      if (fieldType == boolean.class) {
        arrayType = boolean[].class;
      } else if (fieldType == byte.class) {
        arrayType = byte[].class;
      } else if (fieldType == short.class) {
        arrayType = short[].class;
      } else if (fieldType == int.class) {
        arrayType = int[].class;
      } else if (fieldType == long.class) {
        arrayType = long[].class;
      } else if (fieldType == float.class) {
        arrayType = float[].class;
      } else if (fieldType == double.class) {
        arrayType = double[].class;
      } else {
        arrayType = Object[].class;
      }

      return (int) (maxOffset + unsafe.arrayIndexScale(arrayType));
    }

    return 0;
  }
}
