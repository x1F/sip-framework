package one.x1f.sip.foundation.testkit.configurationproperties.models;

import static one.x1f.sip.foundation.core.util.SIPExchangeHelper.filterNonSerializableHeaders;

import java.util.Map;
import lombok.Data;
import org.apache.camel.Exchange;
import org.apache.camel.support.MessageHelper;

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
    messageProperties.setBody(MessageHelper.extractBodyAsString(exchange.getMessage()));
    messageProperties.setHeaders(filterNonSerializableHeaders(exchange));
    return messageProperties;
  }
}
