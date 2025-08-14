package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

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
              if (executionResult.getMessage().getHeader(key) == null
                  || !RegexUtil.compare(
                      (String) value, executionResult.getMessage().getHeader(key, String.class))) {
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
