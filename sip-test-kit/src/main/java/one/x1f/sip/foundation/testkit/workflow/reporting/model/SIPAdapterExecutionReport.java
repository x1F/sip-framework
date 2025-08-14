package one.x1f.sip.foundation.testkit.workflow.reporting.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import one.x1f.sip.foundation.testkit.configurationproperties.models.MessageProperties;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;

@Data
public class SIPAdapterExecutionReport {
  private Exchange actualResponse;
  private Exchange expectedResponse;
  private Map<String, Object> validatedHeaders = new HashMap<>();
  private List<ValidationResult> validationResults = new ArrayList<>();
  private String adapterExceptionMessage;

  @Setter(AccessLevel.PRIVATE)
  private MessageProperties responseMessage;

  public void setActualResponse(Exchange actualResponse) {
    this.actualResponse = actualResponse;
    this.setResponseMessage(MessageProperties.mapToMessageProperties(actualResponse));
  }

  public SIPAdapterExecutionReport setAdapterExceptionMessage(Exception exception) {
    adapterExceptionMessage = exception != null ? exception.toString() : null;
    return this;
  }
}
