package one.x1f.sip.foundation.core.scope.conversation;

import static one.x1f.sip.foundation.core.scope.conversation.notifiers.ConversationCreatedExchangeEventNotifier.SCOPE_PROPERTY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.support.EventHelper;
import org.springframework.stereotype.Component;

/** Keeps track of staring exchange and all that were crated from it */
@Component
public class ConversationTracker {
  private final Map<String, Set<String>> breadcrumbs = new HashMap<>();

  public void registerExchange(String key, String exchangeId) {
    breadcrumbs.computeIfAbsent(key, k -> new HashSet<>());
    breadcrumbs.get(key).add(exchangeId);
  }

  public void deregisterExchange(Exchange exchange) {
    String key = exchange.getProperty(SCOPE_PROPERTY, String.class);
    Set<String> crumbs = breadcrumbs.get(key);
    crumbs.remove(exchange.getExchangeId());
    if (crumbs.isEmpty()) {
      breadcrumbs.remove(key);
      sendConversationCompletedEvent(exchange);
    }
  }

  private void sendConversationCompletedEvent(Exchange exchange) {
    exchange.setProperty("finalExchangeOnRoute", true);
    EventHelper.notifyExchangeDone(exchange.getContext(), exchange);
  }
}
