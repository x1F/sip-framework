package one.x1f.sip.foundation.testkit.configurationproperties.models;

import static one.x1f.sip.foundation.core.util.SIPExchangeHelper.filterNonSerializableHeaders;

import java.util.Map;
import lombok.Data;
import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import org.apache.camel.Exchange;

@Data
public class ResultMessage {
  private String body;
  private Map<String, Object> headers;

  /**
   * Creates a {@link ResultMessage} from the {@link Exchange}
   *
   * @param exchange that should be mapped
   * @return serializable result message
   */
  public static ResultMessage mapToResultMessage(Exchange exchange) {
    ResultMessage messageProperties = new ResultMessage();
    messageProperties.setBody(TestKitHelper.extractBodyAsJsonString(exchange.getMessage()));
    messageProperties.setHeaders(filterNonSerializableHeaders(exchange));
    return messageProperties;
  }
}
