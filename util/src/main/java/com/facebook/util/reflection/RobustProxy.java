package com.facebook.util.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RobustProxy {
  public static <T> T wrap(Class<T> clazz, final T instance) {
    InvocationHandler handler = new InvocationHandler() { @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (instance != null) {
          return method.invoke(instance, args);
        } else if (method.getReturnType().equals(Void.class)) {
          return null;
        } else { // instance == null and caller expects a value
//          return wrap(method.getReturnType(), null);
          throw new UnsupportedOperationException();
        }
      }
    };
//    Class proxyClass = Proxy.getProxyClass( clazz.getClassLoader(), new Class[]{clazz});
//    T wrapper = (T) proxyClass.getConstructor(new Class[]{InvocationHandler.class}).newInstance(new Object[]{handler});

    T wrapper = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);

    return wrapper;
  }
}
