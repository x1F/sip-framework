package one.x1f.sip.foundation.testkit.workflow.thenphase.validator;

import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Validator interface representing a validation command */
@Component
public interface ExchangeValidator {

  /**
   * Execute validation
   *
   * @param executionResult  Result of test execution
   * @param expectedResponse Expected result of test execution
   * @param executionId
   * @return {@link ValidationResult}
   */
  ValidationResult execute(Exchange executionResult, Exchange expectedResponse, UUID executionId);

  /**
   * Checks whether Validator is applicable based on parameters
   *
   * @param executionResult Result of test execution
   * @param expectedResponse Expected result of test execution
   * @return true if Validator is applicable, otherwise false
   */
  boolean isApplicable(Exchange executionResult, Exchange expectedResponse);
}
