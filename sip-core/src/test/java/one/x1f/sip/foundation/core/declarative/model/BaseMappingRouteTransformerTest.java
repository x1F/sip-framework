package one.x1f.sip.foundation.core.declarative.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorType;
import one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BaseMappingRouteTransformerTest {

  static InboundConnectorDefinition inboundConnectorDefinition =
      mock(InboundConnectorDefinition.class);
  static OutboundConnectorDefinition outboundConnectorDefinition =
      mock(OutboundConnectorDefinition.class);
  static IntegrationScenarioDefinition integrationScenarioDefinition =
      mock(IntegrationScenarioDefinition.class);

  ModelMapper<Integer, Integer> integerModelMapper =
      new ModelMapper<>() {
        @Override
        public Class<Integer> getSourceModelClass() {
          return Integer.class;
        }

        @Override
        public Class<Integer> getTargetModelClass() {
          return Integer.class;
        }

        @Override
        public Integer mapToTargetModel(Integer sourceModel) {
          return sourceModel;
        }
      };

  static RouteDefinition routeDefinition = new RouteDefinition();
  ResponseMappingRouteTransformer<Integer, Integer> inboundResponseTransformerUnderTest =
      new ResponseMappingRouteTransformer<>(
          () -> inboundConnectorDefinition,
          () -> integrationScenarioDefinition,
          integerModelMapper);

  ResponseMappingRouteTransformer<Integer, Integer> outboundResponseTransformerUnderTest =
      new ResponseMappingRouteTransformer<>(
          () -> outboundConnectorDefinition,
          () -> integrationScenarioDefinition,
          integerModelMapper);

  @BeforeAll
  static void setUp() {

    Mockito.<Class<?>>when(integrationScenarioDefinition.getRequestModelClass())
        .thenReturn(Integer.class);
    Mockito.when(integrationScenarioDefinition.getResponseModelClass())
        .thenReturn(Optional.of(String.class));

    when(inboundConnectorDefinition.getId()).thenReturn("inbound-connector");
    when(inboundConnectorDefinition.getConnectorType()).thenReturn(ConnectorType.IN);
    Mockito.<Class<?>>when(inboundConnectorDefinition.getRequestModelClass())
        .thenReturn(String.class);
    Mockito.when(inboundConnectorDefinition.getResponseModelClass())
        .thenReturn(Optional.of(Integer.class));

    when(outboundConnectorDefinition.getId()).thenReturn("outbound-connector");
    when(outboundConnectorDefinition.getConnectorType()).thenReturn(ConnectorType.OUT);
    Mockito.<Class<?>>when(outboundConnectorDefinition.getRequestModelClass())
        .thenReturn(Integer.class);
    Mockito.when(outboundConnectorDefinition.getResponseModelClass())
        .thenReturn(Optional.of(Integer.class));

    // mock non-existing global mapper
    CamelContext camelContext = mock(CamelContext.class, RETURNS_DEEP_STUBS);
    routeDefinition.setCamelContext(camelContext);
    DeclarationsRegistry declarationsRegistry = mock(DeclarationsRegistry.class);
    when(camelContext.getRegistry().findSingleByType(DeclarationsRegistry.class))
        .thenReturn(declarationsRegistry);
  }

  @Test
  void WHEN_incompatibleSourceMappersUsed_THEN_SIPExceptionIsThrown() {
    // act&assert
    assertThatThrownBy(() -> inboundResponseTransformerUnderTest.accept(routeDefinition))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Mapper '%s' %s type '%s' is not compatible with assigned type '%s' of connector '%s'",
            integerModelMapper.getClass().getName(),
            "source",
            integerModelMapper.getSourceModelClass().getName(),
            inboundResponseTransformerUnderTest.getSourceModelClass().getName(),
            inboundConnectorDefinition.getId());
  }

  @Test
  void WHEN_incompatibleTargetMappersUsed_THEN_SIPExceptionIsThrown() {
    // act&assert
    assertThatThrownBy(() -> outboundResponseTransformerUnderTest.accept(routeDefinition))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Mapper '%s' %s type '%s' is not compatible with assigned type '%s' of connector '%s'",
            integerModelMapper.getClass().getName(),
            "target",
            integerModelMapper.getTargetModelClass().getName(),
            outboundResponseTransformerUnderTest.getTargetModelClass().getName(),
            outboundConnectorDefinition.getId());
  }
}
