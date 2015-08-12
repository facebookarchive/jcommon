package com.facebook.memory.data.types.definitions;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;

/**
 * hack that allows a sequence of statically declared final fields to function as a "struct" definition for a class
 *
 * a slot is very similar to a field in a C struct. However, we allow variable-sized fields
 *
 * eg a byte array could be
 *    | length - 4 | length bytes of data |
 *
 * Struct + slots allow packing of these
 *
 * slot 0 : | ptr - 8 |
 * slot 1 : | length - 4 | key bytes |
 * slot 2 : | length - 4 | value bytes |
 *
 * access can be achieved
 *   slot0.accessor(address).method_on_pointer();
 *   slot1.accessor(address).method_on_bytes();
 *   slot2.accessor(address).method_on_bytes();
 *
 * and all offset calculations are done automatically
 *
 * notes:
 *   -this assumes that the data length does not change (very similar to C structs)
 *   -this is optimized for extensibility and data footprint. If one wants a layout such as:
 *
 *  | key length | value length | key bytes | value bytes |
 *
 * custom slots for arrays of arrays can be implemented (TODO)
 *
 */
public abstract class Slot {
  static final ConcurrentMap<Class<?>, Struct> STRUCT_MAP = Maps.newConcurrentMap();

  private final SlotOffsetMapper slotOffsetMapper;

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  protected Slot(SlotType slotType) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    Struct struct = findOrCreateStruct(stackTrace);

    if (struct == null) {
      throw new RuntimeException("your class hierachy doesn't include any OffHeapStructure, does it?");
    }

    Struct.SlotInfo lastSlotInfo = struct.getLastSlotInfo();

    // we construct this Slot's OffsetMapper by wrapping the prevous Slot's
    if (lastSlotInfo != null) {
      slotOffsetMapper = new RelativeSlotOffsetMapper(
        lastSlotInfo.getSlotOffsetMapper(), slotType.getSlotSizeFunction()
      );
    } else {
      slotOffsetMapper = new RelativeSlotOffsetMapper(slotType.getSlotSizeFunction());
    }
    // store this slot, its type, and SlotOffsetManager
    struct.updateStruct(this, slotType, slotOffsetMapper);
  }

  protected SlotOffsetMapper getSlotOffsetMapper() {
    return slotOffsetMapper;
  }

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
    Class<?> currentClass = clazz.getSuperclass();

    while (!currentClass.equals(Object.class)) {
      if (isFrameFromOffHeapStructure(currentClass)) {
        Struct parentStruct = STRUCT_MAP.get(currentClass);

        return parentStruct;
      }

      currentClass = currentClass.getSuperclass();
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
