package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.testkit.config.AdapterConfigurationProperties;
import one.x1f.sip.foundation.testkit.exception.NoRouteInvokerException;
import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.*;
import org.apache.camel.*;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.rest.RestEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;

class RouteInvokerFactoryTest {

  private static final String ROUTE_ID = "routeId";

  private RouteInvokerFactory subject;
  private Endpoint endpoint;
  private Exchange exchange;
  private Route route;

  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    RestRouteInvoker restRouteInvoker =
        new RestRouteInvoker(
            camelContext,
            mock(Environment.class),
            restTemplateBuilder,
            new AdapterConfigurationProperties());
    FileRouteInvoker fileRouteInvoker = new FileRouteInvoker(camelContext);
    FtpRouteInvoker ftpRouteInvoker = new FtpRouteInvoker(camelContext);
    JmsRouteInvoker jmsRouteInvoker = new JmsRouteInvoker(camelContext);
    DirectRouteInvoker directRouteInvoker =
        new DirectRouteInvoker(
            mock(ProducerTemplate.class),
            camelContext,
            mock(DeclarationsRegistry.class),
            new ObjectMapper());
    Set<RouteInvoker> invokers =
        Set.of(
            restRouteInvoker,
            mock(CxfRouteInvoker.class),
            fileRouteInvoker,
            ftpRouteInvoker,
            jmsRouteInvoker,
            directRouteInvoker);
    subject = new RouteInvokerFactory(invokers, camelContext);
    exchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    exchange.setProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);
    route = mock(Route.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
  }

  @Test
  void GIVEN_restEndpoint_WHEN_resolveAndInvoke_THEN_getRestRouteInvoker()
      throws NoRouteInvokerException {
    // arrange
    endpoint = mock(RestEndpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act
    RouteInvoker actual = subject.getInstance(exchange);

    // assert
    assertThat(actual).isInstanceOf(RestRouteInvoker.class);
  }

  @Test
  void GIVEN_fileEndpoint_WHEN_resolveAndInvoke_THEN_getFileRouteInvoker()
      throws NoRouteInvokerException {
    // arrange
    endpoint = mock(FileEndpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act
    RouteInvoker actual = subject.getInstance(exchange);

    // assert
    assertThat(actual).isInstanceOf(FileRouteInvoker.class);
  }

  @Test
  void GIVEN_ftpEndpoint_WHEN_resolveAndInvoke_THEN_getFtpRouteInvoker()
      throws NoRouteInvokerException {
    // arrange
    endpoint = mock(RemoteFileEndpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act
    RouteInvoker actual = subject.getInstance(exchange);

    // assert
    assertThat(actual).isInstanceOf(FtpRouteInvoker.class);
  }

  @Test
  void GIVEN_jmsEndpoint_WHEN_resolveAndInvoke_THEN_getJmsRouteInvoker()
      throws NoRouteInvokerException {
    // arrange
    endpoint = mock(JmsEndpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act
    RouteInvoker actual = subject.getInstance(exchange);

    // assert
    assertThat(actual).isInstanceOf(JmsRouteInvoker.class);
  }

  @Test
  void GIVEN_directEndpoint_WHEN_resolveAndInvoke_THEN_getDirectRouteInvoker()
      throws NoRouteInvokerException {
    // arrange
    endpoint = mock(DirectEndpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act
    RouteInvoker actual = subject.getInstance(exchange);

    // assert
    assertThat(actual).isInstanceOf(DirectRouteInvoker.class);
  }

  @Test
  void
      GIVEN_endpointWithoutRouteInvoker_WHEN_resolveAndInvoke_THEN_expectNoRouteInvokerException() {
    // arrange
    endpoint = mock(Endpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    // act & assert
    assertThrows(NoRouteInvokerException.class, () -> subject.getInstance(exchange));
  }
}
