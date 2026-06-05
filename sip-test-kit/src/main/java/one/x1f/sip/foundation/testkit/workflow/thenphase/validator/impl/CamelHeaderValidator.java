package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.EVAL_PREFIX;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.evaluateValidationScript;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import one.x1f.sip.foundation.testkit.util.RegexUtil;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/** Validator for headers of a request in Camel */
@Component
@AllArgsConstructor
public class CamelHeaderValidator implements ExchangeValidator {

  /**
   * Invokes compare header content
   *
   * @param executionResult Result of test execution
   * @param expectedResponse Expected result of test execution
   * @return {@link ValidationResult}
   */
  @Override
  public ValidationResult execute(Exchange executionResult, Exchange expectedResponse) {
    AtomicBoolean result = new AtomicBoolean(true);
    expectedResponse
        .getMessage()
        .getHeaders()
        .forEach(
            (key, value) -> {
              String expectedHeaderValue =
                  expectedResponse.getMessage().getHeader(key, String.class);
              String actualHeaderValue = executionResult.getMessage().getHeader(key, String.class);
              if (expectedHeaderValue == null) {
                result.set(false);
              } else if (expectedHeaderValue.startsWith(EVAL_PREFIX)) {
                var evaluationResult =
                    evaluateValidationScript(
                        expectedHeaderValue.substring(EVAL_PREFIX.length()), actualHeaderValue);

                result.set(evaluationResult.isSuccess());
              } else if (!RegexUtil.compare((String) value, actualHeaderValue)) {
                result.set(false);
              }
            });

    return new ValidationResult(
        result.get(),
        result.get() ? "Header validation successful" : "Header validation unsuccessful");
  }

  @Override
  public boolean isApplicable(Exchange executionResult, Exchange expectedResponse) {
    return expectedResponse != null && !expectedResponse.getMessage().getHeaders().isEmpty();
  }
}
