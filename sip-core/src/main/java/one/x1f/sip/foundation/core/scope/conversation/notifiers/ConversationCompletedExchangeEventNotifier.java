package one.x1f.sip.foundation.core.scope.conversation.notifiers;

import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.scope.conversation.ConversationContextHolder;
import one.x1f.sip.foundation.core.scope.conversation.ConversationTracker;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.AbstractExchangeEvent;
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.impl.event.ExchangeFailedEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationCompletedExchangeEventNotifier extends EventNotifierSupport {
  private final ConversationTracker conversationTracker;
  private final ConversationContextHolder conversationHolder;

  public void notify(CamelEvent event) {
    AbstractExchangeEvent abstractExchangeEvent = (AbstractExchangeEvent) event;
    Exchange exchange = abstractExchangeEvent.getExchange();
    conversationHolder.removeBean();
    conversationTracker.deregisterExchange(exchange);
  }

  @Override
  public boolean isEnabled(CamelEvent event) {
    return event instanceof ExchangeCompletedEvent || event instanceof ExchangeFailedEvent;
  }
}
