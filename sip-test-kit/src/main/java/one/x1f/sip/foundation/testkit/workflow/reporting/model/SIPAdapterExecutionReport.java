package one.x1f.sip.foundation.testkit.workflow.reporting.model;

import java.util.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import one.x1f.sip.foundation.testkit.configurationproperties.models.ResultMessage;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;

@Data
public class SIPAdapterExecutionReport {
  private Exchange actualResponse;
  private Exchange expectedResponse;
  private Map<String, Object> validatedHeaders = new HashMap<>();
  private List<ValidationResult> validationResults = new ArrayList<>();
  private String adapterExceptionMessage;
  private UUID executionId;

  @Setter(AccessLevel.PRIVATE)
  private ResultMessage responseMessage;

  public void setActualResponse(Exchange actualResponse) {
    this.actualResponse = actualResponse;
    this.setResponseMessage(ResultMessage.mapToResultMessage(actualResponse));
  }

  public SIPAdapterExecutionReport setAdapterExceptionMessage(Exception exception) {
    adapterExceptionMessage = exception != null ? exception.toString() : null;
    return this;
  }
}
