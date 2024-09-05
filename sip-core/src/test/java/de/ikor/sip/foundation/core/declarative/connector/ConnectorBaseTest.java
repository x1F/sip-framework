package de.ikor.sip.foundation.core.declarative.connector;

import static org.assertj.core.api.Assertions.assertThat;

import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
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

  @OutboundConnector(
      connectorGroup = "group",
      integrationScenario = "scenario",
      requestModel = Object.class)
  public class TestConnector extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("log");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return super.defineTransformationOrchestrator();
    }
  }
}
