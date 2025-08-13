package one.x1f.sip.foundation.core.actuator.routes;

/**
 * Represents an operation that accepts two input arguments and returns no result. Created for usage
 * with Apache Camel
 *
 * @param <T> - param1
 * @param <R> - param2
 */
@FunctionalInterface
interface CheckedBiConsumer<T, R> {
  /**
   * Performs this operation on the given arguments.
   *
   * @param t the first input argument
   * @param r the second input argument
   * @throws Exception possible exception due to Apache Camel operations
   */
  void consume(T t, R r) throws Exception;
}
