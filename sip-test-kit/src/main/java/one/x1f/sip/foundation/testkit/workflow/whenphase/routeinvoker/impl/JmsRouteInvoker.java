package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.SIPJmsTextMessage.*;

import jakarta.jms.*;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.SIPJmsTextMessage;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.RouteInvokerException;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.UnsupportedJmsHeaderException;
import org.apache.camel.*;
import org.apache.camel.component.jms.*;
import org.apache.camel.support.AsyncProcessorConverterHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/** Invoker class for triggering routes with Jms consumer */
@Component
@ConditionalOnClass(JmsComponent.class)
@RequiredArgsConstructor
@Slf4j
public class JmsRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;

  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    JmsConsumer consumer = (JmsConsumer) resolveConsumer(inputExchange, camelContext);
    AsyncProcessor processor = AsyncProcessorConverterHelper.convert(consumer.getProcessor());

    Exchange exchange = createJmsExchange(consumer, inputExchange);

    try {
      processor.process(exchange);
    } catch (Exception e) {
      throw new RouteInvokerException(this.getClass().getName());
    }
    return Optional.empty();
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof JmsEndpoint;
  }

  @Override
  public boolean shouldSuspend(Endpoint endpoint) {
    return true;
  }

  private Exchange createJmsExchange(JmsConsumer consumer, Exchange inputExchange) {
    Exchange jmsExchange = consumer.createExchange(false);
    JmsBinding binding = consumer.getEndpoint().getBinding();
    jmsExchange.setProperty(Exchange.BINDING, binding);

    TextMessage textMessage = new SIPJmsTextMessage((String) inputExchange.getMessage().getBody());

    jmsExchange.setIn(new JmsMessage(jmsExchange, textMessage, null, binding));
    prepareCustomHeaders(textMessage, inputExchange.getMessage().getHeaders());
    return jmsExchange;
  }

  private void prepareCustomHeaders(TextMessage textMessage, Map<String, Object> headers) {
    headers.forEach(
        (key, value) -> {
          try {
            if (!shouldAvoidSpecificHeader(key)) {
              textMessage.setObjectProperty(key, value);
            } else {
              throw new UnsupportedJmsHeaderException(key);
            }
          } catch (JMSException e) {
            log.error("sip.testkit.workflow.whenphase.routeinvoker.jms.badheader_{}", key);
          }
        });
  }

  private boolean shouldAvoidSpecificHeader(String key) {
    return key.equals(JMS_DESTINATION)
        || key.equals(JMS_REPLY_TO)
        || key.equals(JMS_CORRELATION_ID_AS_BYTES);
  }
}
