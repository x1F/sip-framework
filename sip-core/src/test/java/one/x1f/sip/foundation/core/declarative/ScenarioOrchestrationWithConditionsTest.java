package one.x1f.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.ScenarioOrchestratedWithConditionsAdapter;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.EndpointInject;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(classes = {ScenarioOrchestratedWithConditionsAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:out")
@DirtiesContext
class ScenarioOrchestrationWithConditionsTest {

  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:out")
  private MockEndpoint mockedLoggerConnector;

  @BeforeEach
  void setup() {
    mockedLoggerConnector.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedLoggerConnector.assertIsSatisfied();
  }

  @Test
  void WHEN_settingOneAsBodyPayload_THEN_responseContainsFirstOutboundConnector() {
    final var exchange = template.withBody("one").to(direct("orchestrate")).send();
    assertThat(exchange.getException()).isNull();
    final var response =
        exchange
            .getMessage()
            .getBody(ScenarioOrchestratedWithConditionsAdapter.OrchestratedResponseModel.class);
    assertThat(response.getCalledConsumers())
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .contains(
            ScenarioOrchestratedWithConditionsAdapter.OrchestratedOutboundConnectorOne.class
                .getSimpleName());
    assertThat(response.isHeaderMode()).isFalse();
    mockedLoggerConnector.expectedMessageCount(1);
  }

  @Test
  void WHEN_settingRandomBodyPayload_THEN_responseContainsSecondOutboundConnector() {
    final var exchange =
        template
            .withBody(RandomStringUtils.insecure().nextAlphanumeric(10))
            .to(direct("orchestrate"))
            .send();
    assertThat(exchange.getException()).isNull();
    final var response =
        exchange
            .getMessage()
            .getBody(ScenarioOrchestratedWithConditionsAdapter.OrchestratedResponseModel.class);
    assertThat(response.getCalledConsumers())
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .contains(
            ScenarioOrchestratedWithConditionsAdapter.OrchestratedOutboundConnectorTwo.class
                .getSimpleName());
    assertThat(response.isHeaderMode()).isFalse();
    mockedLoggerConnector.expectedMessageCount(1);
  }

  @Test
  void WHEN_settingEmptyPayload_THEN_exceptionForUnhandledRequestIsTriggered() {
    final var exchange = template.withBody(Strings.EMPTY).to(direct("orchestrate")).send();
    assertThat(exchange.getException()).isInstanceOf(SIPFrameworkException.class);
    assertThat(exchange.getException())
        .hasMessageContaining("No integration-scenario consumer was called during orchestration");
    mockedLoggerConnector.expectedMessageCount(0);
  }

  @Test
  void WHEN_settingEmptyPayloadWithHeader_THEN_responseHasHeaderInfoSetToTrue() {
    final var exchange =
        template
            .withBody(Strings.EMPTY)
            .withHeader(ScenarioOrchestratedWithConditionsAdapter.HEADER_MODE, true)
            .to(direct("orchestrate"))
            .send();
    assertThat(exchange.getException()).isNull();
    final var response =
        exchange
            .getMessage()
            .getBody(ScenarioOrchestratedWithConditionsAdapter.OrchestratedResponseModel.class);
    assertThat(response.isHeaderMode()).isTrue();
    assertThat(response.getCalledConsumers())
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .hasSize(1);
    mockedLoggerConnector.expectedMessageCount(0);
  }

  @Test
  void
      WHEN_settingOnePayloadWithHeader_THEN_responseContainsFirstConnectorAndHasHeaderInfoSetToTrue() {
    final var exchange =
        template
            .withBody("one")
            .withHeader(ScenarioOrchestratedWithConditionsAdapter.HEADER_MODE, true)
            .to(direct("orchestrate"))
            .send();
    assertThat(exchange.getException()).isNull();
    final var response =
        exchange
            .getMessage()
            .getBody(ScenarioOrchestratedWithConditionsAdapter.OrchestratedResponseModel.class);
    assertThat(response.isHeaderMode()).isTrue();
    assertThat(response.getCalledConsumers())
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .containsExactlyInAnyOrder(
            ScenarioOrchestratedWithConditionsAdapter.OrchestratedOutboundConnectorHeaderMode.class
                .getSimpleName(),
            ScenarioOrchestratedWithConditionsAdapter.OrchestratedOutboundConnectorOne.class
                .getSimpleName());
    mockedLoggerConnector.expectedMessageCount(0);
  }
}
