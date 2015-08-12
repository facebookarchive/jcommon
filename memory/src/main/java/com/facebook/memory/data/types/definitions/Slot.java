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

  private final FieldOffsetMapper fieldOffsetMapper;

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  protected Slot(FieldType fieldType) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    Struct struct = findOrCreateStruct(stackTrace);

    if (struct == null) {
      throw new RuntimeException("your class hierachy doesn't include any OffHeapStructure, does it?");
    }

    Struct.Field lastField = struct.getLastField();

    if (lastField != null) {
      fieldOffsetMapper = new RelativeFieldOffsetMapper(
        lastField.getFieldOffsetMapper(), fieldType.getFieldSizeFunction()
      );
    } else {
      fieldOffsetMapper = new RelativeFieldOffsetMapper(fieldType.getFieldSizeFunction());
    }

    struct.updateStruct(fieldType, fieldOffsetMapper);
  }

  protected FieldOffsetMapper getFieldOffsetMapper() {
    return fieldOffsetMapper;
  }

  public int getOffset(long address) {
    return fieldOffsetMapper.getFieldStartOffset(address);
  }

  public abstract T accessor(long address);

  /**
   * this is a faster method to create an accessor based on the previous slot/field in the struct
   *
   * @param previousSlotAccess previously bound to the base adddress
   * @return
   */
  public abstract T accessor(SlotAccessor previousSlotAccessor);

  private static Struct findOrCreateStruct(StackTraceElement[] stackTrace) {
    for (StackTraceElement element : stackTrace) {
      Context context = getContext(element);
      Class<?> clazz = context.getClazz();
      String methodName = context.getMethodName();

      if (methodName.equals("<clinit>")) {
        if (isFrameFromOffHeapStructure(clazz)) {
          Struct parentStruct = findParentStruct(clazz);
          Struct struct = STRUCT_MAP.computeIfAbsent(clazz, c -> new Struct(clazz, parentStruct));

          return struct;
        }
      }
    }

    return null;
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

    findOrCreateStruct(Thread.currentThread().getStackTrace());
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
