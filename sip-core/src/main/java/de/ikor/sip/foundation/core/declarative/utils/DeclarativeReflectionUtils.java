package de.ikor.sip.foundation.core.declarative.utils;

import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Helper methods that rely on Java Reflection.
 *
 * <p><em>Intended for internal use only</em>
 */
@UtilityClass
public class DeclarativeReflectionUtils {

  public static <A extends Annotation> A getAnnotationOrThrow(Class<A> annotation, Object from) {
    var ann = from.getClass().getAnnotation(annotation);
    if (null == ann) {
      throw SIPFrameworkInitializationException.init(
          "Annotation @%s required on class %s", annotation.getSimpleName(), from.getClass());
    }
    return ann;
  }

  public static <A extends Annotation> Optional<A> getAnnotationIfPresent(
      Class<A> annotation, Object from) {
    return Optional.ofNullable(from.getClass().getAnnotation(annotation));
  }

  @SneakyThrows
  public static <T> T createInstance(Class<T> clazz, Object... parameters)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
          IllegalAccessException {
    Class<?>[] params = Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new);
    return clazz.getConstructor(params).newInstance(parameters);
  }

  public static Class<?> getClassFromGeneric(Class<?> clazz, Class<?> abstractSuperclass) {
    return (Class<?>) traverseHierarchyTree(clazz, abstractSuperclass).getActualTypeArguments()[0];
  }

  private static ParameterizedType traverseHierarchyTree(Class<?> clazz, Class<?> superClass) {
    if (clazz.getSuperclass().equals(superClass)) {
      return (ParameterizedType) clazz.getGenericSuperclass();
    } else {
      return traverseHierarchyTree(clazz.getSuperclass(), superClass);
    }
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static <T> T invokeMethod(
      final Object instance, final String methodName, final Object... params) {
    return (T)
        instance
            .getClass()
            .getMethod(
                methodName, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new))
            .invoke(instance, params);
  }

  @SneakyThrows
  public static <T> T invokeConstructor(final Class<T> clazz, final Object... params) {
    return clazz
        .getConstructor(Arrays.stream(params).map(Object::getClass).toArray(Class[]::new))
        .newInstance(params);
  }
}
