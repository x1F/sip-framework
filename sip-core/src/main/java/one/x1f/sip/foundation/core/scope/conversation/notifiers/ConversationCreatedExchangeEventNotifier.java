package one.x1f.sip.foundation.core.scope.conversation.notifiers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.scope.conversation.ConversationContextHolder;
import one.x1f.sip.foundation.core.scope.conversation.ConversationTracker;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.ExchangeCreatedEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationCreatedExchangeEventNotifier extends EventNotifierSupport {
  public static final String SCOPE_PROPERTY = "scopeKey";
  private final ConversationTracker conversationTracker;
  private final ConversationContextHolder contextHolder;

  public void notify(CamelEvent event) {
    ExchangeCreatedEvent ece = (ExchangeCreatedEvent) event;
    Exchange exchange = ece.getExchange();
    String key = exchange.getProperty(SCOPE_PROPERTY, String.class);
    if (key == null) {
      key = exchange.getExchangeId();
      exchange.setProperty(SCOPE_PROPERTY, key);
    }
    contextHolder.setConversationId(key);
    conversationTracker.registerExchange(key, exchange.getExchangeId());
  }

  @Override
  public boolean isEnabled(CamelEvent event) {
    return event instanceof ExchangeCreatedEvent;
  }
}
