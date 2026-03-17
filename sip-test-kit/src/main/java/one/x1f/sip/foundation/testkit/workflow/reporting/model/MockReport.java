package one.x1f.sip.foundation.testkit.workflow.reporting.model;

import static one.x1f.sip.foundation.testkit.configurationproperties.models.ResultMessage.mapToResultMessage;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import one.x1f.sip.foundation.testkit.configurationproperties.models.ResultMessage;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;

/** Report for a single Mock */
@Data
@Accessors(chain = true)
public class MockReport {
  private EndpointValidationOutcome validated = EndpointValidationOutcome.SKIPPED;
  private Exchange expected;
  private ResultMessage expectedMessage;
  private Exchange actual;
  private ResultMessage actualMessage;
  private Map<String, Object> validatedHeaders;
  private List<ValidationResult> validationResults;

  public MockReport setExpected(Exchange expected) {
    this.expected = expected;
    this.setExpectedMessage(mapToResultMessage(expected));
    return this;
  }
}
