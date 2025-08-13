package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;

@UtilityClass
public class ScenarioContextPredicates {

  /**
   * Test if the content of a header exists and is equal to the expected value
   *
   * @param headerName Name of the header
   * @param expectedValue Expected value to test for
   * @return <code>true</code> if this header exists and is of expected value
   * @param <T> Header type
   */
  public static <T, M> Predicate<ScenarioOrchestrationContext<M>> headerEquals(
      final String headerName, final T expectedValue) {
    Objects.requireNonNull(expectedValue);
    return context ->
        context
            .getHeader(headerName, expectedValue.getClass())
            .map(expectedValue::equals)
            .orElse(false);
  }

  /**
   * Tests if a specific header exists
   *
   * @param headerName Name of the header
   * @return <code>true</code> if a header with that name exists
   */
  public static <M> Predicate<ScenarioOrchestrationContext<M>> hasHeader(final String headerName) {
    return context -> context.getHeader(headerName, Object.class).isPresent();
  }

  /**
   * Tests the original request using the given predicate
   *
   * @param <T> Request type
   * @param requestType Request type class
   * @param requestPredicate Predicate to test against the original request
   * @return Test result
   */
  public static <T, M> Predicate<ScenarioOrchestrationContext<M>> originalRequestMatches(
      final Class<T> requestType, final Predicate<T> requestPredicate) {
    return context -> requestPredicate.test(context.getOriginalRequest(requestType));
  }

  /**
   * Tests the current response against the given predicate
   *
   * @see ScenarioOrchestrationContext#getResponse()
   * @param responsePredicate Predicate to test response against
   * @return Test result
   * @param <M> Response model type
   */
  public static <M> Predicate<ScenarioOrchestrationContext<M>> responseMatches(
      final Predicate<M> responsePredicate) {
    return context -> context.getResponse().map(responsePredicate::test).orElse(false);
  }
}
