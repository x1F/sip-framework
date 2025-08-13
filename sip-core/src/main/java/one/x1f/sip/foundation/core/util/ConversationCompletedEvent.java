package one.x1f.sip.foundation.core.util;

import static one.x1f.sip.foundation.core.scope.conversation.notifiers.ConversationCreatedExchangeEventNotifier.SCOPE_PROPERTY;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.impl.event.AbstractExchangeEvent;

public class ConversationCompletedEvent extends AbstractExchangeEvent {
  @Getter @Setter private String conversationId;

  public ConversationCompletedEvent(Exchange source) {
    super(source);
    conversationId = source.getProperty(SCOPE_PROPERTY, String.class);
  }

  @Override
  public Type getType() {
    return Type.Custom;
  }
}
