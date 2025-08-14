package one.x1f.sip.foundation.core.scope.conversation;

import java.util.HashMap;
import java.util.Map;
import one.x1f.sip.foundation.core.util.ConversationCompletedEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

@Component
public class ConversationContextHolder extends EventNotifierSupport {
  private final ThreadLocal<String> conversationIdHolder =
      new NamedThreadLocal<>("Conversation Context");

  protected final Map<String, Map<String, Object>> scopeBeans = new HashMap<>();

  private ConversationContextHolder() {}

  public void setConversationId(String id) {
    conversationIdHolder.set(id);
    scopeBeans.putIfAbsent(id, new HashMap<>());
  }

  public void removeBean() {
    conversationIdHolder.remove();
  }

  public String get() {
    return conversationIdHolder.get();
  }

  public Object getOrCreateScopedBean(String beanName, ObjectFactory<?> factory) {
    return scopeBeans.get(get()).computeIfAbsent(beanName, b -> factory.getObject());
  }

  @Override
  public void notify(CamelEvent event) {
    ConversationCompletedEvent conversationCompletedEvent = (ConversationCompletedEvent) event;
    scopeBeans.remove(conversationCompletedEvent.getConversationId());
  }

  @Override
  public boolean isEnabled(CamelEvent event) {
    return event instanceof ConversationCompletedEvent;
  }
}
