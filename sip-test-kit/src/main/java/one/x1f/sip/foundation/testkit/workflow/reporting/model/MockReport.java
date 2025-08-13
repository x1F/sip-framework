package one.x1f.sip.foundation.testkit.workflow.reporting.model;

import static one.x1f.sip.foundation.testkit.configurationproperties.models.MessageProperties.mapToMessageProperties;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import one.x1f.sip.foundation.testkit.configurationproperties.models.MessageProperties;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;

/** Report for a single Mock */
@Data
@Accessors(chain = true)
public class MockReport {
  private EndpointValidationOutcome validated = EndpointValidationOutcome.SKIPPED;
  private Exchange expected;
  private MessageProperties expectedMessage;
  private Exchange actual;
  private MessageProperties actualMessage;
  private Map<String, Object> validatedHeaders;
  private List<ValidationResult> validationResults;

  public MockReport setExpected(Exchange expected) {
    this.expected = expected;
    this.setExpectedMessage(mapToMessageProperties(expected));
    return this;
  }
}
