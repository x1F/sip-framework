package one.x1f.sip.foundation.core.util;

import static java.lang.Boolean.TRUE;

import org.apache.camel.Exchange;
import org.apache.camel.impl.event.DefaultEventFactory;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.ExchangeHelper;

public class ExtendedEventFactory extends DefaultEventFactory {
  @Override
  public CamelEvent createExchangeCompletedEvent(Exchange exchange) {
    String finalExchangeDone =
        ExchangeHelper.getHeaderOrProperty(exchange, "finalExchangeOnRoute", String.class);

    if (TRUE.toString().equals(finalExchangeDone)) {
      return new ConversationCompletedEvent(exchange);
    } else {
      return super.createExchangeCompletedEvent(exchange);
    }
  }
}
