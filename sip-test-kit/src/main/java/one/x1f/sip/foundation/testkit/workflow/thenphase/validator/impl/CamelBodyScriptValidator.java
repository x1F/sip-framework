package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static org.apache.camel.support.MessageHelper.extractBodyAsString;
import static org.apache.camel.support.MessageHelper.resetStreamCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/** Validator for body of a request in Camel */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelBodyScriptValidator implements ExchangeValidator {
  /**
   * Invokes compare body content
   *
   * @param actualResult Result of test execution
   * @param expectedResponse Expected result of test execution
   * @return {@link ValidationResult}
   */
  @Override
  public ValidationResult execute(Exchange actualResult, Exchange expectedResponse) {
    resetStreamCache(actualResult.getMessage());
    String expected = extractBodyAsString(expectedResponse.getMessage());
    String actual = extractBodyAsString(actualResult.getMessage());
    return evaluateValidationScript(expected.substring(EVAL_PREFIX.length()), actual);
  }

  @Override
  public boolean isApplicable(Exchange executionResult, Exchange expectedResponse) {
    if (expectedResponse == null) return false;
    String bodyAsString = extractBodyAsString(expectedResponse.getMessage());
    return bodyAsString != null && bodyAsString.startsWith(EVAL_PREFIX);
  }
}
