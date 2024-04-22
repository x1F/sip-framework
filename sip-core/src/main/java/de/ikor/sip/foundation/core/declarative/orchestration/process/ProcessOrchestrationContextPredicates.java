package de.ikor.sip.foundation.core.declarative.orchestration.process;

import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

/** Utilities for writing the conditionals in the DSL */
@UtilityClass
public class ProcessOrchestrationContextPredicates {

  /**
   * Test if the specific header is equal to the given value
   *
   * @param headerName - Name of the header to be checked
   * @param expectedValue - expected value
   * @param valueType - type of the expected value
   * @return {@code true} if a header has that value
   */
  public static CompositeProcessStepConditional headerEquals(
      final String headerName, final Object expectedValue, Class<?> valueType) {
    Objects.requireNonNull(expectedValue);
    return context ->
        context.getHeader(headerName, valueType).map(expectedValue::equals).orElse(false);
  }

  /**
   * Tests if a specific header exists
   *
   * @param headerName Name of the header
   * @return {@code true} if a header with that name exists
   */
  public static CompositeProcessStepConditional hasHeader(final String headerName) {
    return context -> context.getHeader(headerName, Object.class).isPresent();
  }

  /**
   * Tests the original request using the given predicate
   *
   * @param requestPredicate Predicate to test against the original request
   * @return {@code true} if the request matches
   */
  public static CompositeProcessStepConditional originalRequestMatches(
      final Predicate<?> requestPredicate) {
    return context -> requestPredicate.test(context.getOriginalRequest());
  }

  /**
   * Tests the current response against the given predicate
   *
   * @see ScenarioOrchestrationContext#getResponse()
   * @param responsePredicate Predicate to test response against
   * @return {@code true} if the response matches
   */
  public static CompositeProcessStepConditional responseMatches(
      final Predicate<Object> responsePredicate) {
    return context ->
        (boolean) context.getLatestResponse().map(responsePredicate::test).orElse(false);
  }
}
