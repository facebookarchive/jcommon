package com.facebook.memory.data.types.definitions;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the idea here is that
 * @param <T>
 */
public abstract class Slot<T extends SlotAccessor> {
  static final ConcurrentMap<Class<?>, AtomicInteger> OFFSETS_BY_STRUCTURE =
    Maps.newConcurrentMap();

  private final int offset;
  private final FieldType fieldType;

  protected Slot(FieldType fieldType) {
    this.fieldType = fieldType;
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
          int ancestorSize = findAncestorSize(clazz);

          AtomicInteger value = new AtomicInteger(ancestorSize);
          AtomicInteger existing = OFFSETS_BY_STRUCTURE.putIfAbsent(clazz, value);

          if (existing == null) {
            existing = value;
          }

          existing.getAndAdd(fieldType.getSize());

          offsetToUse = existing.get();
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

  private static int findAncestorSize(Class<?> clazz) {
    Class<?> superClazz = clazz.getSuperclass();

    while (!superClazz.equals(Object.class)) {
      if (isFrameFromOffHeapStructure(superClazz)) {
        AtomicInteger superClazzSize = OFFSETS_BY_STRUCTURE.get(superClazz);

        return superClazzSize.get();
      }
    }

    return 0;
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
