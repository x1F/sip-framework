package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvoker.CONNECTOR_ID_EXCHANGE_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import org.apache.camel.*;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

public class DirectRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String CONNECTOR_ID = "connectorId";
  public static final String JSON_MODEL_PAYLOAD_BODY = "{\"name\":\"Freddy\",\"years\":42}";

  public record Person(String name, int years) {}

  private DirectRouteInvoker subject;

  private ProducerTemplate producerTemplate;

  private Endpoint endpoint;

  private ConnectorMock connector;

  private DeclarationsRegistry declarationsRegistry;

  private Exchange inputExchange;

  public static class ConnectorMock extends GenericInboundConnectorBase {
    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return null;
    }
  }

  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    producerTemplate = mock(ProducerTemplate.class);
    declarationsRegistry = mock(DeclarationsRegistry.class);
    JsonMapper mapper = new JsonMapper();
    subject = new DirectRouteInvoker(producerTemplate, camelContext, declarationsRegistry, mapper);

    Route route = mock(Route.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    endpoint = mock(Endpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);

    inputExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    inputExchange.setProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, CONNECTOR_ID);
    inputExchange.setProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);
    inputExchange.getMessage().setBody(JSON_MODEL_PAYLOAD_BODY);

    connector = mock(ConnectorMock.class);
    when(producerTemplate.send(endpoint, inputExchange)).thenReturn(inputExchange);
  }

  @Test
  void
      GIVEN_connectorWithJsonRequestModelAsPerson_WHEN_invoke_THEN_verifyProducerCallAndSendPersonPayload() {
    // arrange
    when(declarationsRegistry.getConnectorById(CONNECTOR_ID_EXCHANGE_PROPERTY))
        .thenReturn(Optional.of(connector));
    doReturn(Person.class).when(connector).getRequestModelClass();

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(inputExchange.getMessage().getBody()).isInstanceOf(Person.class);
    verify(producerTemplate, times(1)).send(endpoint, inputExchange);
  }

  @Test
  void GIVEN_noConnector_WHEN_invoke_THEN_verifyProducerCallAndSendStringPayload() {
    // arrange
    when(declarationsRegistry.getConnectorById(CONNECTOR_ID_EXCHANGE_PROPERTY))
        .thenReturn(Optional.empty());

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(inputExchange.getMessage().getBody()).isInstanceOf(String.class);
    verify(producerTemplate, times(1)).send(endpoint, inputExchange);
  }
}
