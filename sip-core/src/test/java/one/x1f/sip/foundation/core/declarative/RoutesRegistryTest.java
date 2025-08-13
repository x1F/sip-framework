package one.x1f.sip.foundation.core.declarative;

import static one.x1f.sip.foundation.core.declarative.RoutesRegistry.SIP_SCENARIO_ORCHESTRATOR_PREFIX;
import static one.x1f.sip.foundation.core.declarative.RoutesRegistry.SIP_SOAP_SERVICE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.dto.RouteDeclarativeStructureInfo;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.proxies.ProcessorProxyRegistry;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.*;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.component.servlet.ServletConsumer;
import org.apache.camel.processor.Enricher;
import org.apache.camel.processor.PollEnricher;
import org.apache.camel.processor.SendProcessor;
import org.apache.camel.spi.CamelEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoutesRegistryTest {

  private static final String CONNECTOR_ID = "connectorId";
  private static final String SCENARIO_ID = "scenarioId";
  private static final String GENERATED_ROUTE_ID = "sip-connector_connectorId_externalEndpoint";
  private static final String SOAP_SERVICE_NAME = "customer";
  private static final String GENERATED_SOAP_ROUTE_ID =
      String.format("%s_%s", SIP_SOAP_SERVICE_PREFIX, SOAP_SERVICE_NAME);
  private static final String BASE_HTTP_URI = "http://baseUri";
  private RoutesRegistry subject;
  private DeclarationsRegistry declarationsRegistry;
  private DeclarationsRegistryTest.InboundConnectorMock connector;

  @BeforeEach
  void setup() {
    declarationsRegistry = mock(DeclarationsRegistry.class);
    subject = new RoutesRegistry(declarationsRegistry);
    connector = mock(DeclarationsRegistryTest.InboundConnectorMock.class);
    when(connector.getId()).thenReturn(CONNECTOR_ID);
  }

  @Test
  void GIVEN_connectorId_WHEN_getRouteIdByConnectorId_THEN_getProperRouteId() {
    // arrange
    subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector);
    when(declarationsRegistry.getConnectorById(CONNECTOR_ID))
        .thenReturn(Optional.ofNullable(connector));

    // act
    String actualRouteId =
        subject.getRouteIdByConnectorIdAndRole(CONNECTOR_ID, RouteRole.EXTERNAL_ENDPOINT);

    // assert
    assertThat(actualRouteId).isEqualTo(GENERATED_ROUTE_ID);
  }

  @Test
  void GIVEN_nonExistentConnectorId_WHEN_getRouteIdByConnectorId_THEN_expectNull() {
    // arrange
    when(declarationsRegistry.getConnectorById(CONNECTOR_ID))
        .thenReturn(Optional.ofNullable(connector));

    // act
    String actualRouteId =
        subject.getRouteIdByConnectorIdAndRole(CONNECTOR_ID, RouteRole.EXTERNAL_ENDPOINT);

    // assert
    assertThat(actualRouteId).isNull();
  }

  @Test
  void GIVEN_nonExistentConnector_WHEN_getRouteIdByConnectorId_THEN_expectNull() {
    // act
    String actualRouteId =
        subject.getRouteIdByConnectorIdAndRole(CONNECTOR_ID, RouteRole.EXTERNAL_ENDPOINT);

    // assert
    assertThat(actualRouteId).isNull();
  }

  @Test
  void GIVEN_connectorId_WHEN_getConnectorIdByRouteId_THEN_connectorIDReturned() {
    // arrange
    subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector);

    // act
    String connectorId =
        subject.getConnectorIdByRouteId(
            "sip-connector_connectorId_" + RouteRole.EXTERNAL_ENDPOINT.roleSuffixInRouteId);

    // assert
    assertThat(connectorId).isEqualTo(CONNECTOR_ID);
  }

  @Test
  void
      GIVEN_alreadyExistingConnectorId_WHEN_generateRouteIdForConnector_THEN_SIPFrameworkInitializationException() {
    // arrange
    subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector);

    // act & assert
    assertThatThrownBy(
            () -> subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Internal SIP Error - Can't build internal connector route with ID '%s': Route already exists",
            GENERATED_ROUTE_ID);
  }

  @Test
  void
      GIVEN_alreadyExistingScenarioOrchestration_WHEN_generateRouteIdForScenarioOrchestrator_THEN_SIPFrameworkInitializationException() {
    // arrange
    IntegrationScenarioDefinition scenario = mock(IntegrationScenarioDefinition.class);
    when(scenario.getId()).thenReturn(SCENARIO_ID);
    final String TEST_SUFFIX = "test-suffix";
    subject.generateRouteIdForScenarioOrchestrator(scenario, TEST_SUFFIX);

    // act & assert
    assertThatThrownBy(
            () -> subject.generateRouteIdForScenarioOrchestrator(scenario, "test-suffix"))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Internal SIP Error - Can't build internal orchestrator route with ID '%s': Route already exists",
            SIP_SCENARIO_ORCHESTRATOR_PREFIX + "_" + SCENARIO_ID + "_" + TEST_SUFFIX);
  }

  @Test
  void GIVEN_soapService_WHEN_generateRouteIdForSoapService_THEN_getProperRouteId() {
    // act
    String actualRouteId = subject.generateRouteIdForSoapService(SOAP_SERVICE_NAME);

    // assert
    assertThat(actualRouteId).isEqualTo(GENERATED_SOAP_ROUTE_ID);
  }

  @Test
  void
      GIVEN_existingSoapService_WHEN_generateRouteIdForSoapService_THEN_SIPFrameworkInitializationException() {
    // arrange
    subject.generateRouteIdForSoapService(SOAP_SERVICE_NAME);

    // act & assert
    assertThatThrownBy(() -> subject.generateRouteIdForSoapService(SOAP_SERVICE_NAME))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Internal SIP Error - Can't build internal soap-service route with ID '%s': Route already exists",
            GENERATED_SOAP_ROUTE_ID);
  }

  @Test
  void GIVEN_endpoint_WHEN_generateRouteInfoList_THEN_expectValidRouteDeclarativeStructureInfo() {
    // arrange
    subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector);
    CamelEvent.CamelContextEvent event = mock(CamelEvent.CamelContextEvent.class);
    CamelContext camelContext = mock(CamelContext.class);
    ExtendedCamelContext extendedCamelContext = mock(ExtendedCamelContext.class);
    ProcessorProxyRegistry proxyRegistry = mock(ProcessorProxyRegistry.class);
    when(proxyRegistry.getProxies()).thenReturn(new HashMap<>());
    when(event.getContext()).thenReturn(camelContext);
    when(camelContext.getCamelContextExtension()).thenReturn(extendedCamelContext);
    when(extendedCamelContext.getContextPlugin(ProcessorProxyRegistry.class))
        .thenReturn(proxyRegistry);
    List<Route> routes = new ArrayList<>();
    Route route = mock(Route.class);
    when(route.getRouteId()).thenReturn(GENERATED_ROUTE_ID);

    Endpoint endpoint = mock(Endpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpointBaseUri()).thenReturn(BASE_HTTP_URI);

    // SendProcessor case
    SendProcessor sendProcessor = mock(SendProcessor.class);
    when(sendProcessor.getEndpoint()).thenReturn(endpoint);

    List<Service> services = List.of(mock(ServletConsumer.class), sendProcessor);
    when(route.getServices()).thenReturn(services);
    routes.add(route);
    when(camelContext.getRoutes()).thenReturn(routes);

    // act
    subject.notify(event);
    List<RouteDeclarativeStructureInfo> actualEndpointAware =
        subject.generateRouteInfoList(endpoint);

    // assert
    assertThat(actualEndpointAware.get(0).getConnectorId()).isEqualTo(CONNECTOR_ID);
  }

  @Test
  void
      GIVEN_endpointAndSpecificProcessors_WHEN_generateRouteInfoList_THEN_expectValidRouteDeclarativeStructureInfo() {
    // ARRANGE
    subject.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, connector);
    CamelEvent.CamelContextEvent event = mock(CamelEvent.CamelContextEvent.class);
    CamelContext camelContext = mock(CamelContext.class);
    ExtendedCamelContext extendedCamelContext = mock(ExtendedCamelContext.class);
    ProcessorProxyRegistry proxyRegistry = mock(ProcessorProxyRegistry.class);
    when(proxyRegistry.getProxies()).thenReturn(new HashMap<>());
    when(event.getContext()).thenReturn(camelContext);
    when(camelContext.getCamelContextExtension()).thenReturn(extendedCamelContext);
    when(extendedCamelContext.getContextPlugin(ProcessorProxyRegistry.class))
        .thenReturn(proxyRegistry);
    List<Route> routes = new ArrayList<>();
    Route route = mock(Route.class);
    when(route.getRouteId()).thenReturn(GENERATED_ROUTE_ID);

    Endpoint endpoint = mock(Endpoint.class);
    when(route.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpointBaseUri()).thenReturn(BASE_HTTP_URI);

    // Enricher case
    Endpoint enricherEndpoint1 = mock(Endpoint.class);
    when(enricherEndpoint1.getEndpointBaseUri()).thenReturn("uriForEnricher");
    Enricher enricher1 = mock(Enricher.class);
    when(enricher1.getExpression())
        .thenReturn(ExpressionBuilder.simpleExpression("uriForEnricher"));

    // Enricher case (without expression and with generated uri)
    Endpoint enricherEndpoint2 = mock(Endpoint.class);
    when(enricherEndpoint2.getEndpointBaseUri()).thenReturn("enrich-1");
    Enricher enricher2 = mock(Enricher.class);

    // PollEnricher case
    Endpoint pollEnricherEndpoint1 = mock(Endpoint.class);
    when(pollEnricherEndpoint1.getEndpointBaseUri()).thenReturn("uriForPollEnricher");
    PollEnricher pollEnricher1 = mock(PollEnricher.class);
    when(pollEnricher1.getExpression())
        .thenReturn(ExpressionBuilder.simpleExpression("uriForPollEnricher"));

    // PollEnricher case (without expression and with generated uri)
    Endpoint pollEnricherEndpoint2 = mock(Endpoint.class);
    when(pollEnricherEndpoint2.getEndpointBaseUri()).thenReturn("pollEnrich-1");
    PollEnricher pollEnricher2 = mock(PollEnricher.class);

    List<Service> services = List.of(enricher1, enricher2, pollEnricher1, pollEnricher2);
    when(route.getServices()).thenReturn(services);
    routes.add(route);
    when(camelContext.getRoutes()).thenReturn(routes);

    // ACT
    subject.notify(event);
    // Enricher case
    List<RouteDeclarativeStructureInfo> actualEnricher1 =
        subject.generateRouteInfoList(enricherEndpoint1);
    // Enricher case (without expression and with generated uri)
    List<RouteDeclarativeStructureInfo> actualEnricher2 =
        subject.generateRouteInfoList(enricherEndpoint2);
    // PollEnricher case
    List<RouteDeclarativeStructureInfo> actualPollEnricher1 =
        subject.generateRouteInfoList(pollEnricherEndpoint1);
    // PollEnricher case (without expression and with generated uri)
    List<RouteDeclarativeStructureInfo> actualPollEnricher2 =
        subject.generateRouteInfoList(pollEnricherEndpoint2);

    // ASSERT
    assertThat(actualEnricher1.get(0).getConnectorId()).isEqualTo(CONNECTOR_ID);
    assertThat(actualEnricher2.get(0).getConnectorId()).isEqualTo(CONNECTOR_ID);
    assertThat(actualPollEnricher1.get(0).getConnectorId()).isEqualTo(CONNECTOR_ID);
    assertThat(actualPollEnricher2.get(0).getConnectorId()).isEqualTo(CONNECTOR_ID);
  }
}
