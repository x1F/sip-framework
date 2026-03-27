package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import org.apache.camel.*;
import org.apache.camel.component.mail.MailConsumer;
import org.apache.camel.component.mail.MailEndpoint;
import org.apache.camel.component.mail.MailMessage;
import org.apache.camel.support.EmptyAsyncCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MailRouteInvokerTest {

  private static final String NODE_ID = "nodeId";
  MailRouteInvoker subject;
  CamelContext camelContext;

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    subject = new MailRouteInvoker(camelContext);
  }

  @Test
  void When_invoke_Expect_ExchangeProcessingInvoked() {
    // arrange
    Exchange exchange = mock(Exchange.class, RETURNS_DEEP_STUBS);
    Exchange mailExchange = mock(Exchange.class);
    when(exchange.getContext()).thenReturn(camelContext);
    Route route = mock(Route.class);
    MailConsumer mailConsumer = mock(MailConsumer.class);
    AsyncProcessor asyncProcessor = mock(AsyncProcessor.class);
    when(exchange.getProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, String.class))
        .thenReturn(NODE_ID);
    when(camelContext.getRoute(NODE_ID)).thenReturn(route);
    when(route.getConsumer()).thenReturn(mailConsumer);
    when(mailConsumer.getAsyncProcessor()).thenReturn(asyncProcessor);
    when(mailConsumer.createExchange(true)).thenReturn(mailExchange);
    when(exchange.getMessage().getBody(String.class)).thenReturn("body");
    Map<String, Object> headers = new HashMap<>();
    headers.put("header 1", "Header value");
    when(exchange.getMessage().getHeaders()).thenReturn(headers);

    // act
    subject.invoke(exchange);

    // assert
    verify(asyncProcessor, times(1)).process(mailExchange, EmptyAsyncCallback.get());
    verify(mailExchange, times(1)).setMessage(any(MailMessage.class));
  }

  @Test
  void When_isApplicable_Expect_True() {
    // arrange
    MailEndpoint endpoint = new MailEndpoint();

    // act + assert
    assertThat(subject.isApplicable(endpoint)).isTrue();
  }

  @Test
  void When_isNotApplicable_Expect_False() {
    // arrange
    Endpoint endpoint = mock(Endpoint.class);

    // act + assert
    assertThat(subject.isApplicable(endpoint)).isFalse();
  }
}
