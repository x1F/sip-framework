package one.x1f.sip.foundation.testkit.configurationproperties.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Class that defines an element of a test. Each element holds a {@link MessageProperties} */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointProperties {
  private String endpointId;

  private String connectorId;

  private MessageProperties requestMessage = new MessageProperties();

  private boolean disableMock;

  public void setWith(MessageProperties message) {
    this.setRequestMessage(message);
  }

  public void setReturning(MessageProperties message) {
    this.setRequestMessage(message);
  }

  public void setHaving(MessageProperties message) {
    this.setRequestMessage(message);
  }
}
