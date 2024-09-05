package de.ikor.sip.foundation.core.declarative.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ConnectorBaseTest {

  @Test
  void When_defineTransformationOrchestrator_Expect_Orchestrator() {
    // arrange
    TestConnector testConnector = new TestConnector();

    // act + assert
    assertThat(ReflectionTestUtils.getField(testConnector, "modelTransformationOrchestrator"))
        .isNull();
    assertThat(testConnector.getOrchestrator()).isNotNull();
    assertThat(ReflectionTestUtils.getField(testConnector, "modelTransformationOrchestrator"))
        .isNotNull();
  }

  @Test
  void When_defineTransformationOrchestrator_With_ForbiddenTransformer_Expect_Exception() {
    // arrange
    ConnectorWithRequestMapperAndTransformer testConnectorRequest =
        new ConnectorWithRequestMapperAndTransformer();
    ConnectorWithResponseMapperAndTransformer testConnectorResponse =
        new ConnectorWithResponseMapperAndTransformer();

    // act + assert
    assertThatThrownBy(() -> testConnectorRequest.getOrchestrator())
        .isInstanceOf(SIPFrameworkInitializationException.class);
    assertThatThrownBy(() -> testConnectorResponse.getOrchestrator())
        .isInstanceOf(SIPFrameworkInitializationException.class);
  }

  @OutboundConnector(
      connectorGroup = "group",
      integrationScenario = "scenario",
      requestModel = Object.class)
  class TestConnector extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("log");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this).setRequestRouteTransformer(this::setRequest);
    }

    private void setRequest(RouteDefinition routeDefinition) {
      routeDefinition.log("log");
    }
  }

  @OutboundConnector(
      connectorGroup = "group",
      integrationScenario = "scenario",
      requestModel = Object.class)
  @UseRequestModelMapper(ModelMapper.class)
  class ConnectorWithRequestMapperAndTransformer extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("log");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this).setRequestRouteTransformer(this::setRequest);
    }

    private void setRequest(RouteDefinition routeDefinition) {
      routeDefinition.log("log");
    }
  }

  @OutboundConnector(
      connectorGroup = "group",
      integrationScenario = "scenario",
      requestModel = Object.class)
  @UseResponseModelMapper(ModelMapper.class)
  class ConnectorWithResponseMapperAndTransformer extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("log");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this).setRequestRouteTransformer(this::setRequest);
    }

    private void setRequest(RouteDefinition routeDefinition) {
      routeDefinition.log("log");
    }
  }
}
