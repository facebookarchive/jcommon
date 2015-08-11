package com.facebook.memory.data.types.definitions;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;

/**
 * hack to allow automatic increment
 *
 * @param <T>
 */
public abstract class Slot<T extends SlotAccessor> {
  static final ConcurrentMap<Class<?>, Struct> STRUCT_MAP = Maps.newConcurrentMap();

  private final int offset;

  protected Slot(FieldType fieldType) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    offset = updateStructOffset(fieldType, stackTrace);

    if (offset == -1) {
      throw new RuntimeException("your class hierachy doesn't include any OffHeapStructure, does it?");
    }
  }

  private static int updateStructOffset(FieldType fieldType, StackTraceElement[] stackTrace) {
    int offsetToUse = -1;

    for (StackTraceElement element : stackTrace) {
      Context context = getContext(element);
      Class<?> clazz = context.getClazz();
      String methodName = context.getMethodName();

      if (methodName.equals("<clinit>")) {
        if (isFrameFromOffHeapStructure(clazz)) {
          Struct parentStruct = findParentStruct(clazz);
          Struct struct = STRUCT_MAP.computeIfAbsent(clazz, c -> new Struct(clazz, parentStruct));

          if (struct.isTerminated()) {
            throw new RuntimeException("attempt to add Slot after final");
          } else {
            // the current offset in the struct is the next location to use
            offsetToUse = struct.getOffset();
            // now update the next available offset
            struct.updateStruct(fieldType);

            if (fieldType.isTerminal()) {
              struct.terminate();
            }
          }
        }
      }
    }

    if (offsetToUse >= 0) {
      return offsetToUse;
    }

    return -1;
  }

  private static Context getContext(StackTraceElement element) {
    Class<?> clazz = toClass(element.getClassName());

    return new Context(clazz, element.getMethodName());
  }

  private static Struct findParentStruct(Class<?> clazz) {
    Class<?> superClazz = clazz.getSuperclass();

    while (!superClazz.equals(Object.class)) {
      if (isFrameFromOffHeapStructure(superClazz)) {
        Struct parentStruct = STRUCT_MAP.get(superClazz);

        return parentStruct;
      }
    }

    return null;
  }

  private static Class<?> toClass(String className) {
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return clazz;
  }

  public abstract T accessor(long address);

  private static boolean isFrameFromOffHeapStructure(Class<?> clazz) {
    Class<?> currentClazz = clazz;
    while (currentClazz != Object.class) {
      Class<?>[] interfaces = currentClazz.getInterfaces();

      if (interfaces != null && interfaces.length > 0) {
        for (Class<?> interfaze : interfaces) {
          if (OffHeapStructure.class.isAssignableFrom(interfaze)) {
            return true;
          }
        }
      }

      currentClazz = currentClazz.getSuperclass();
    }

    return false;
  }

  public int getOffset() {
    return offset;
  }

  static void forceSizing(Class<? extends OffHeapStructure> clazz) {
    Class<?> currentClazz = clazz;

    while (currentClazz != Object.class) {
      ClassLoader classLoader = currentClazz.getClassLoader();

      try {
        for (Field field : currentClazz.getDeclaredFields()) {
          if (Modifier.isStatic(field.getModifiers())) {
            boolean accessible = field.isAccessible();

            try {
              field.setAccessible(true);
              field.get(currentClazz);
            } finally {
              field.setAccessible(accessible);
            }
          }
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("should never see this");
      }

      currentClazz = currentClazz.getSuperclass();
    }

    updateStructOffset(FieldType.MEASURE, Thread.currentThread().getStackTrace());
  }

  private static class Context {
    private final Class<?> clazz;
    private final String methodName;

    private Context(Class<?> clazz, String methodName) {
      this.clazz = clazz;
      this.methodName = methodName;
    }

    public Class<?> getClazz() {
      return clazz;
    }

    public String getMethodName() {
      return methodName;
    }
  }
}
