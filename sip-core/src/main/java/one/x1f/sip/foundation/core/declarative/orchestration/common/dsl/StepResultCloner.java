package one.x1f.sip.foundation.core.declarative.orchestration.common.dsl;

import java.io.Serializable;
import java.util.function.Function;
import one.x1f.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Interface for cloning a step-result (response) during an orchestration process.
 *
 * <p>Default implementations for common use-cases are provided via {@link #forCloneable()}, {@link
 * #forSerializable()}, and {@link #forCopyConstructor()}.
 *
 * @param <T> Type of the element to be cloned
 */
@FunctionalInterface
public interface StepResultCloner<T> extends Function<T, T> {

  static <T extends Cloneable> StepResultCloner<T> forCloneable() {
    return element -> DeclarativeReflectionUtils.invokeMethod(element, "clone");
  }

  static <T extends Serializable> StepResultCloner<T> forSerializable() {
    return SerializationUtils::clone;
  }

  @SuppressWarnings("unchecked")
  static <T> StepResultCloner<T> forCopyConstructor() {
    return element -> (T) DeclarativeReflectionUtils.invokeConstructor(element.getClass(), element);
  }
}
