package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static org.apache.camel.support.MessageHelper.resetStreamCache;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.TestRunnerContext;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/** Validator for body of a request in Camel */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelBodyScriptValidator implements ExchangeValidator {

  private final TestRunnerContext context;

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
    return evaluateValidationScript(
        expected.substring(EVAL_PREFIX.length()), actual, context.getOrCreate(executionId));
  }

  @Override
  public boolean isApplicable(Exchange executionResult, Exchange expectedResponse) {
    if (expectedResponse == null) return false;
    String bodyAsString = extractBodyAsJsonString(expectedResponse.getMessage());
    return bodyAsString != null && bodyAsString.startsWith(EVAL_PREFIX);
  }
}
