package one.x1f.sip.foundation.testkit.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import one.x1f.sip.foundation.testkit.exception.UnsuspendedRouteException;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.*;
import org.apache.camel.*;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.jms.JmsConsumer;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.kafka.KafkaConsumer;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.impl.engine.DefaultRouteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;

class CamelContextLifecycleHandlerTest {

  private static final String ROUTE_ID = "routeId";

  private CamelContextLifecycleHandler subject;
  private CamelContext camelContext;
  private DefaultRouteController defaultRouteController;
  private Route route;

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
    defaultRouteController = mock(DefaultRouteController.class);
    when(camelContext.getRouteController()).thenReturn(defaultRouteController);
    List<Route> routes = new ArrayList<>();
    route = mock(Route.class);
    routes.add(route);

    RestRouteInvoker restRouteInvoker =
        new RestRouteInvoker(
            camelContext,
            mock(Environment.class),
            mock(RestTemplateBuilder.class),
            new AdapterConfigurationProperties());
    FileRouteInvoker fileRouteInvoker = new FileRouteInvoker(camelContext);
    FtpRouteInvoker ftpRouteInvoker = new FtpRouteInvoker(camelContext);
    JmsRouteInvoker jmsRouteInvoker = new JmsRouteInvoker(camelContext);
    KafkaRouteInvoker kafkaRouteInvoker = new KafkaRouteInvoker(camelContext);
    List<RouteInvoker> invokers =
        List.of(
            restRouteInvoker,
            mock(CxfRouteInvoker.class),
            fileRouteInvoker,
            ftpRouteInvoker,
            jmsRouteInvoker,
            kafkaRouteInvoker);

    subject = new CamelContextLifecycleHandler(invokers);

    when(camelContext.getRoutes()).thenReturn(routes);
    when(route.getRouteId()).thenReturn(ROUTE_ID);
  }

  @Test
  void GIVEN_routeWithSuspendingFileConsumer_WHEN_afterApplicationStart_THEN_verifySuspendingRoute()
      throws Exception {
    // arrange
    FileConsumer fileConsumer = mock(FileConsumer.class);
    when(route.getConsumer()).thenReturn(fileConsumer);

    // act
    subject.afterApplicationStart(camelContext);

    // assert
    verify(defaultRouteController, times(1)).suspendRoute(ROUTE_ID);
  }

  @Test
  void
      GIVEN_routeWithSuspendingPollingConsumer_WHEN_afterApplicationStart_THEN_verifySuspendingRoute()
          throws Exception {
    // arrange
    PollingConsumer pollingConsumer = mock(PollingConsumer.class);
    when(route.getConsumer()).thenReturn(pollingConsumer);

    // act
    subject.afterApplicationStart(camelContext);

    // assert
    verify(defaultRouteController, times(1)).suspendRoute(ROUTE_ID);
  }

  @Test
  void GIVEN_routeWithSuspendingJmsConsumer_WHEN_afterApplicationStart_THEN_verifySuspendingRoute()
      throws Exception {
    // arrange
    JmsConsumer jmsConsumer = mock(JmsConsumer.class);
    when(route.getConsumer()).thenReturn(jmsConsumer);
    when(jmsConsumer.getEndpoint()).thenReturn(mock(JmsEndpoint.class));

    // act
    subject.afterApplicationStart(camelContext);

    // assert
    verify(defaultRouteController, times(1)).suspendRoute(ROUTE_ID);
  }

  @Test
  void
      GIVEN_routeWithSuspendingKafkaConsumer_WHEN_afterApplicationStart_THEN_verifySuspendingRoute()
          throws Exception {
    // arrange
    KafkaConsumer kafkaConsumer = mock(KafkaConsumer.class);
    when(route.getConsumer()).thenReturn(kafkaConsumer);
    when(kafkaConsumer.getEndpoint()).thenReturn(mock(KafkaEndpoint.class));

    // act
    subject.afterApplicationStart(camelContext);

    // assert
    verify(defaultRouteController, times(1)).suspendRoute(ROUTE_ID);
  }

  @Test
  void
      GIVEN_routeWithNoSuspendingConsumer_WHEN_afterApplicationStart_THEN_verifyNoMatchingConditionForSuspending()
          throws Exception {
    // arrange
    Consumer consumer = mock(Consumer.class);
    when(route.getConsumer()).thenReturn(consumer);
    when(consumer.getEndpoint()).thenReturn(mock(Endpoint.class));

    // act
    subject.afterApplicationStart(camelContext);

    // assert
    verify(defaultRouteController, times(0)).suspendRoute(ROUTE_ID);
  }

  @Test
  void
      GIVEN_routeWithSuspendingException_WHEN_afterApplicationStart_THEN_expectUnsuspendedRouteException()
          throws Exception {
    // arrange
    FileConsumer fileConsumer = mock(FileConsumer.class);
    when(route.getConsumer()).thenReturn(fileConsumer);
    doThrow(new Exception()).when(defaultRouteController).suspendRoute(ROUTE_ID);

    // act & assert
    assertThrows(
        UnsuspendedRouteException.class, () -> subject.afterApplicationStart(camelContext));
  }
}
