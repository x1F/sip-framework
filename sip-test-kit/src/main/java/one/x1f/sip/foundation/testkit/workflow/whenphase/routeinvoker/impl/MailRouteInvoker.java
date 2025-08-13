package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.resolveConsumer;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.mail.MailComponent;
import org.apache.camel.component.mail.MailConsumer;
import org.apache.camel.component.mail.MailEndpoint;
import org.apache.camel.component.mail.MailMessage;
import org.apache.camel.support.EmptyAsyncCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/** Invoker of exchange processing for {@link MailConsumer} */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(MailComponent.class)
public class MailRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;

  @Override
  public Optional<Exchange> invoke(Exchange whenDefinition) {
    MailConsumer mailConsumer = (MailConsumer) resolveConsumer(whenDefinition, camelContext);

    Exchange mailExchange = mailConsumer.createExchange(true);
    mailExchange.setMessage(createMailMessage(whenDefinition));

    mailConsumer.getAsyncProcessor().process(mailExchange, EmptyAsyncCallback.get());
    return Optional.empty();
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof MailEndpoint;
  }

  @Override
  public boolean shouldSuspend(Endpoint endpoint) {
    return true;
  }

  private MailMessage createMailMessage(Exchange whenDefinition) {
    MailMessage mailMessage =
        new MailMessage(whenDefinition, createMimeMessage(whenDefinition), true);
    mailMessage.setBody(whenDefinition.getMessage().getBody());
    mailMessage.setHeaders(whenDefinition.getMessage().getHeaders());
    return mailMessage;
  }

  private MimeMessage createMimeMessage(Exchange whenDefinition) {
    MimeMessage message = new MimeMessage((Session) null);
    setMimeMessageHeaders(message, whenDefinition.getMessage().getHeaders());
    setMimeMessageBody(message, whenDefinition.getMessage().getBody(String.class));
    return message;
  }

  private void setMimeMessageHeaders(MimeMessage message, Map<String, Object> headers) {
    if (headers != null) {
      headers.forEach(
          (key, value) -> {
            try {
              message.setHeader(key, String.valueOf(value));
            } catch (MessagingException e) {
              log.error(e.getMessage());
            }
          });
    }
  }

  private void setMimeMessageBody(MimeMessage message, String body) {
    try {
      message.setText(body);
    } catch (MessagingException e) {
      log.error(e.getMessage());
    }
  }
}
