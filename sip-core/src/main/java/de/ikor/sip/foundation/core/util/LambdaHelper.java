package de.ikor.sip.foundation.core.util;

import java.util.Comparator;
import java.util.function.BiFunction;
import lombok.experimental.UtilityClass;

/** Class providing helpers for use with Lambdas */
@UtilityClass
public class LambdaHelper {

  /**
   * A {@link java.util.Comparator} that allows to be used with implementations throwing checked
   * expceptions.
   *
   * @param <T> Type to compare
   */
  @FunctionalInterface
  public interface ThrowingComparator<T> extends Comparator<T> {

    /**
     * Compares the given objects, possibly throwing a checked exception
     *
     * @param o1 First object to be compared
     * @param o2 Second object to be compared
     * @return Comparison result (see {@link Comparator#compare(Object, Object)})
     */
    int compareWithException(T o1, T o2) throws Exception;

    /**
     * Default {@link Comparator#compare(Object, Object)} that wraps any checked exception into a
     * {@link RuntimeException}. *
     *
     * @see Comparator#compare(Object, Object)
     */
    @Override
    default int compare(T o1, T o2) {
      return compare(o1, o2, RuntimeException::new);
    }

    /**
     * Compares the given objects, wrapping any checked exception using the provided <code>
     * exceptionWrapper</code>.
     *
     * @param o1 First object to compare
     * @param o2 Second object to compare
     * @param exceptionWrapper Wrapper to use for checked exceptions
     * @return Comparison result (see @{@link Comparator#compare(Object, Object)})
     */
    default int compare(
        T o1, T o2, BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
      try {
        return compareWithException(o1, o2);
      } catch (RuntimeException ex) {
        throw ex;
      } catch (Exception ex) {
        throw exceptionWrapper.apply(ex.getMessage(), ex);
      }
    }
  }
}
