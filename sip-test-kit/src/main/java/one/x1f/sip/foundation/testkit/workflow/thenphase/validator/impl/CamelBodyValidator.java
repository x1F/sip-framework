package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.EVAL_PREFIX;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.extractBodyAsJsonString;
import static org.apache.camel.support.MessageHelper.resetStreamCache;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators.*;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/** Validator for body of a request in Camel */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelBodyValidator implements ExchangeValidator {
  public static final String BODY_VALIDATION_UNSUCCESSFUL = "Body validation unsuccessful";
  public static final String BODY_VALIDATION_SUCCESSFUL = "Body validation successful";
  private final List<StringComparator> comparators;

  /**
   * Invokes compare body content
   *
   * @param actualResult Result of test execution
   * @param expectedResponse Expected result of test execution
   * @param executionId
   * @return {@link ValidationResult}
   */
  @Override
  public ValidationResult execute(
      Exchange actualResult, Exchange expectedResponse, UUID executionId) {
    resetStreamCache(actualResult.getMessage());
    String expected = extractBodyAsJsonString(expectedResponse.getMessage());
    String actual = extractBodyAsJsonString(actualResult.getMessage());

    if (areSurelyDifferent(actual, expected)) {
      return new ValidationResult(false, BODY_VALIDATION_UNSUCCESSFUL);
    }

    Map<Boolean, List<ComparatorResult>> resultsByOutcome =
        compareAndGroupResults(expected, actual);

    return isValidationSuccess(resultsByOutcome)
        ? new ValidationResult(true, BODY_VALIDATION_SUCCESSFUL)
        : getFirstFailedOrDefault(
            resultsByOutcome, new ValidationResult(false, BODY_VALIDATION_UNSUCCESSFUL));
  }

  private Map<Boolean, List<ComparatorResult>> compareAndGroupResults(
      String expected, String actual) {
    return comparators.stream()
        .map(comparator -> safeCompare(expected, actual, comparator))
        .filter(comparatorResult -> comparatorResult.getStatus() != null)
        .collect(groupingBy(ComparatorResult::getStatus));
  }

  private ValidationResult getFirstFailedOrDefault(
      Map<Boolean, List<ComparatorResult>> results, ValidationResult defaultResult) {
    return results.get(false).stream()
        .map(this::toValidationResult)
        .findFirst()
        .orElse(defaultResult);
  }

  private static boolean areSurelyDifferent(String actual, String expected) {
    return isNoneBlank(expected) && isBlank(actual);
  }

  @Override
  public boolean isApplicable(Exchange executionResult, Exchange expectedResponse) {
    if (expectedResponse == null) return false;
    String bodyAsString = extractBodyAsJsonString(expectedResponse.getMessage());
    return bodyAsString != null && !bodyAsString.startsWith(EVAL_PREFIX);
  }

  private ValidationResult toValidationResult(ComparatorResult comparatorResult) {
    return new ValidationResult(false, getFailureDescription(comparatorResult));
  }

  private String getFailureDescription(ComparatorResult comparatorResult) {
    String descriptionFromComparator =
        format("%s: %s", BODY_VALIDATION_UNSUCCESSFUL, comparatorResult.getFailureDescription());
    return comparatorResult.getFailureDescription() == null
        ? BODY_VALIDATION_UNSUCCESSFUL
        : descriptionFromComparator;
  }

  private static ComparatorResult safeCompare(
      String expected, String actual, StringComparator comparator) {
    try {
      return comparator.compare(expected, actual);
    } catch (IncompatibleStringComparator e) {
      log.trace(format("Expected: %s %n Actual: %s ", expected, actual), e.getMessage());
      return new ComparatorResult();
    }
  }

  private static boolean isValidationSuccess(
      Map<Boolean, List<ComparatorResult>> validationResults) {
    return validationResults.containsKey(true) && !validationResults.get(true).isEmpty();
  }
}
