package one.x1f.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.annotation.*;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition;
import one.x1f.sip.foundation.core.declarative.model.RequestMappingRouteTransformer;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

class DeclarationsRegistryTest {

  private static final String INBOUND_CONNECTOR_ID = "inboundConnectorId";
  private static final String OUTBOUND_CONNECTOR_ID = "outboundConnectorId";
  private static final String SCENARIO_ID = "scenarioId";
  private static final String CONNECTOR_GROUP_ID = "connectorGroupId";
  private static final String SECOND_SCENARIO_ID = "secondScenarioId";

  private DeclarationsRegistry subject;

  private final List<ConnectorGroupDefinition> connectorGroups = new ArrayList<>();
  private final List<IntegrationScenarioDefinition> scenarios = new ArrayList<>();
  private final List<ConnectorDefinition> connectors = new ArrayList<>();

  private final ApplicationContext applicationContext = mock(ApplicationContext.class);

  private final List<CompositeProcessDefinition> compositeProcessDefinitions = new ArrayList<>();

  public static class ScenarioMock extends IntegrationScenarioBase {}

  public static class InboundConnectorMock extends GenericInboundConnectorBase {
    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return null;
    }
  }

  public static class OutboundConnectorMock extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return null;
    }
  }

  @Test
  void WHEN_checkForUnusedScenarios_THEN_expectSIPFrameworkInitializationException() {
    // arrange
    ScenarioMock firstScenario = mock(ScenarioMock.class);
    when(firstScenario.getId()).thenReturn(SCENARIO_ID);
    scenarios.add(firstScenario);
    // act & assert
    assertThatThrownBy(
            () -> {
              subject =
                  new DeclarationsRegistry(
                      connectorGroups,
                      scenarios,
                      connectors,
                      compositeProcessDefinitions,
                      applicationContext);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Nothing is providing data to the integration scenario with id '%s'", SCENARIO_ID);
  }

  @Test
  void WHEN_checkForDuplicateScenarios_THEN_expectSIPFrameworkInitializationException() {
    // arrange
    IntegrationScenarioDefinition firstScenario = mock(ScenarioMock.class);
    IntegrationScenarioDefinition secondScenario = mock(ScenarioMock.class);
    when(firstScenario.getId()).thenReturn(SCENARIO_ID);
    when(secondScenario.getId()).thenReturn(SCENARIO_ID);
    scenarios.add(firstScenario);
    scenarios.add(secondScenario);

    // act & assert
    assertThatThrownBy(
            () -> {
              subject =
                  new DeclarationsRegistry(
                      connectorGroups,
                      scenarios,
                      connectors,
                      compositeProcessDefinitions,
                      applicationContext);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessageContaining(
            "There is a non-unique %s ID '%s' in class %s. A unique ID should be provided in the element's annotation.",
            "integration scenario", SCENARIO_ID, secondScenario.getClass().getName());
  }

  @Test
  void WHEN_getConnectorById_THEN_expectValidConnector() {
    // arrange
    InboundConnectorMock connector = mock(InboundConnectorMock.class);
    when(connector.getId()).thenReturn(INBOUND_CONNECTOR_ID);
    connectors.add(connector);
    subject =
        new DeclarationsRegistry(
            connectorGroups,
            scenarios,
            connectors,
            compositeProcessDefinitions,
            applicationContext);

    // act
    Optional<ConnectorDefinition> actual = subject.getConnectorById(INBOUND_CONNECTOR_ID);

    // assert
    assertThat(actual).isPresent();
    assertThat(actual.get().getId()).isEqualTo(INBOUND_CONNECTOR_ID);
  }

  @Test
  void WHEN_getScenarioByIdFindsNoScenario_THEN_expectSIPFrameworkInitializationException() {
    // arrange
    IntegrationScenarioDefinition firstScenario = mock(ScenarioMock.class);
    when(firstScenario.getId()).thenReturn(SCENARIO_ID);
    scenarios.add(firstScenario);

    InboundConnectorMock inboundConnector = mock(InboundConnectorMock.class);
    when(inboundConnector.getId()).thenReturn(INBOUND_CONNECTOR_ID);
    when(inboundConnector.getScenarioId()).thenReturn(SCENARIO_ID);
    when(inboundConnector.getConnectorGroupId()).thenReturn(CONNECTOR_GROUP_ID);
    connectors.add(inboundConnector);

    OutboundConnectorMock outboundConnector = mock(OutboundConnectorMock.class);
    when(outboundConnector.getId()).thenReturn(OUTBOUND_CONNECTOR_ID);
    when(outboundConnector.getScenarioId()).thenReturn(SCENARIO_ID);
    when(outboundConnector.getConnectorGroupId()).thenReturn(CONNECTOR_GROUP_ID);
    connectors.add(outboundConnector);

    subject =
        new DeclarationsRegistry(
            connectorGroups,
            scenarios,
            connectors,
            compositeProcessDefinitions,
            applicationContext);

    // act & assert
    assertThatThrownBy(() -> subject.getScenarioById(SECOND_SCENARIO_ID))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage("There is no integration scenario with id: %s", SECOND_SCENARIO_ID);
  }

  @Test
  void WHEN_AnnotatedClass_WITH_NoParent_THEN_SIPFrameworkInitializationExceptionThrown() {

    Map.of(
            IntegrationScenario.class,
            "IntegrationScenarioBase",
            ConnectorGroup.class,
            "ConnectorGroupBase",
            InboundConnector.class,
            "InboundConnectorBase",
            OutboundConnector.class,
            "GenericOutboundConnectorBase",
            CompositeProcess.class,
            "CompositeProcessBase")
        .forEach(
            (annotationClass, baseClass) -> {
              reset(applicationContext);
              when(applicationContext.getBeansWithAnnotation(annotationClass))
                  .thenReturn(Map.of("key", 1));

              assertThatThrownBy(
                      () -> {
                        subject =
                            new DeclarationsRegistry(
                                connectorGroups,
                                scenarios,
                                connectors,
                                compositeProcessDefinitions,
                                applicationContext);
                      })
                  .isInstanceOf(SIPFrameworkInitializationException.class)
                  .hasMessage(
                      "Annotated %s java.lang.Integer is not inheriting %s parent class or any of it's child classes. Please inherit the proper class.",
                      annotationClass.getSimpleName(), baseClass);
            });
  }

  @Test
  void WHEN_CheckingConnectorMapping_WITH_NoMappingsOrTransformation_THEN_NoExceptionThrown() {
    // arrange
    InboundConnectorMock connector = mock(InboundConnectorMock.class);
    when(connector.getConnectorGroupId()).thenReturn(CONNECTOR_GROUP_ID);
    connectors.add(connector);

    // assert
    assertDoesNotThrow(
        () -> {
          subject =
              new DeclarationsRegistry(
                  connectorGroups,
                  scenarios,
                  connectors,
                  compositeProcessDefinitions,
                  applicationContext);
        });
  }

  @Test
  void WHEN_CheckingConnectorMapping_WITH_MappingsAndNoTransformation_THEN_NoExceptionThrown() {
    // arrange
    InboundConnectorMock connector = mock(InboundConnectorMock.class);
    RequestMappingRouteTransformer<Object, Object> routeTransformer =
        RequestMappingRouteTransformer.forConnectorWithScenario(
            connector,
            () -> {
              return null;
            },
            null);
    ConnectorOrchestrator connectorOrchestrator = mock(ConnectorOrchestrator.class);
    when(connector.getId()).thenReturn("mockConnector");
    when(connectorOrchestrator.getRequestRouteTransformer()).thenReturn(routeTransformer);
    when(connector.getOrchestrator()).thenReturn(connectorOrchestrator);
    connectors.add(connector);

    // assert
    assertDoesNotThrow(
        () -> {
          subject =
              new DeclarationsRegistry(
                  connectorGroups,
                  scenarios,
                  connectors,
                  compositeProcessDefinitions,
                  applicationContext);
        });
  }
}
