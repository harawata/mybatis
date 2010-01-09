package org.apache.ibatis.executor.loader;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.Serializable;
import java.lang.reflect.Method;

public class ResultObjectProxy {

  private static final TypeHandlerRegistry registry = new TypeHandlerRegistry();

  public static Object createProxy(Class type, Object target, ResultLoaderRegistry lazyLoader) {
    return EnhancedResultObjectProxyImpl.createProxy(type, target, lazyLoader);
  }

  private static class EnhancedResultObjectProxyImpl implements InvocationHandler, Serializable {

    private Object target;
    private ResultLoaderRegistry lazyLoader;

    private EnhancedResultObjectProxyImpl(Object target, ResultLoaderRegistry lazyLoader) {
      this.target = target;
      this.lazyLoader = lazyLoader;
    }

    public static Object createProxy(Class type, Object target, ResultLoaderRegistry lazyLoader) {
      if (registry.hasTypeHandler(type)) {
        return target;
      } else {
        return Enhancer.create(type, new EnhancedResultObjectProxyImpl(target, lazyLoader));
      }
    }

    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
      try {
        if (!Object.class.equals(method.getDeclaringClass()) && PropertyNamer.isGetter(method.getName())) {
            lazyLoader.loadAll();
        }
        return method.invoke(target, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
  }

}
