package one.x1f.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.ScenarioOrchestrationAdapter;
import one.x1f.sip.foundation.core.apps.declarative.ScenarioOrchestrationAdapter.AutoOrchestratedOutboundConnectorOne;
import one.x1f.sip.foundation.core.apps.declarative.ScenarioOrchestrationAdapter.AutoOrchestratedOutboundConnectorTwo;
import one.x1f.sip.foundation.core.apps.declarative.ScenarioOrchestrationAdapter.ScenarioResponse;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(classes = {ScenarioOrchestrationAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class ScenarioOrchestrationTest {

  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:messageConnector1")
  private MockEndpoint mockedLoggerConnector1;

  @EndpointInject("mock:log:messageConnector2")
  private MockEndpoint mockedLoggerConnector2;

  @EndpointInject("mock:log:messageConnector3")
  private MockEndpoint mockedLoggerConnector3;

  @BeforeEach
  void setup() {
    mockedLoggerConnector1.reset();
    mockedLoggerConnector2.reset();
    mockedLoggerConnector3.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedLoggerConnector1.assertIsSatisfied();
    mockedLoggerConnector2.assertIsSatisfied();
    mockedLoggerConnector3.assertIsSatisfied();
  }

  @Test
  void WHEN_callingFirstOrSecondInboundConnector_THEN_OnlyFirstOutboundConnectorReceivesAMessage() {
    // arrange
    mockedLoggerConnector1.expectedBodiesReceivedInAnyOrder(
        "Hi Adapter-scenarioprepared", "Hi Adapter-scenarioprepared");
    mockedLoggerConnector2.expectedMessageCount(0);
    mockedLoggerConnector3.expectedMessageCount(0);

    // act
    Exchange exchangeFirstConnector =
        template.withBody("Hi Adapter").to(direct("dummyInputOne")).send();
    ScenarioResponse responseFirstConnector =
        exchangeFirstConnector.getMessage().getBody(ScenarioResponse.class);
    Exchange exchangeSecondConnector =
        template.withBody("Hi Adapter").to(direct("dummyInputTwo")).send();
    ScenarioResponse responseSecondConnector =
        exchangeSecondConnector.getMessage().getBody(ScenarioResponse.class);

    // assert
    assertThat(exchangeFirstConnector.getException()).isNull();
    assertThat(exchangeSecondConnector.getException()).isNull();
    assertThat(responseFirstConnector).isInstanceOf(ScenarioResponse.class);
    assertThat(responseFirstConnector.getValue()).isEqualTo(1);
    assertThat(responseSecondConnector).isEqualTo(responseFirstConnector);
    assertThat(exchangeFirstConnector.getMessage().getHeader("headerKey"))
        .isEqualTo("Header set in scenario orchestration");
  }

  @Test
  void
      WHEN_callingSecondInboundConnector_THEN_OutboundConnector1And2ReceiveAMessageAndResponseIsAggregated() {
    // arrange
    mockedLoggerConnector1.expectedBodiesReceived("Hi Adapter");
    mockedLoggerConnector2.expectedBodiesReceived("Hi Adapter-scenarioprepared");

    // act
    Exchange exchange = template.withBody("Hi Adapter").to(direct("dummyInputThree")).send();

    // assert
    assertThat(exchange.getException()).isNull();
    assertThat(exchange.getMessage().getBody()).isInstanceOf(ScenarioResponse.class);
    ScenarioResponse response = exchange.getMessage().getBody(ScenarioResponse.class);
    assertThat(response.getValue()).isEqualTo(3120);
    assertThat(response.getId()).isEqualTo("scenario-handled-response");
  }

  @Test
  void WHEN_callingUsnpecifiedInboundConnector_THEN_ResponseIsAggregated() {
    // arrange
    mockedLoggerConnector1.expectedBodiesReceived("Hi Adapter");
    mockedLoggerConnector2.expectedBodiesReceived("Hi Adapter");
    mockedLoggerConnector3.expectedBodiesReceived("Hi Adapter");

    // act
    Exchange exchange = template.withBody("Hi Adapter").to(direct("dummyInputFour")).send();

    // assert
    assertThat(exchange.getException()).isNull();
    assertThat(exchange.getMessage().getBody()).isInstanceOf(ScenarioResponse.class);
    ScenarioResponse response = exchange.getMessage().getBody(ScenarioResponse.class);
    assertThat(response.getValue()).isEqualTo(6);
    assertThat(response.getId()).isEqualTo("scenario-aggregated-response");
  }

  @Test
  void WHEN_callingAutoOrchestratedScenario_THEN_MessagesAreProperlyReceived() {

    // arrange
    String payload = "Hi Adapter-";
    mockedLoggerConnector1.expectedMessageCount(1);
    mockedLoggerConnector1.expectedBodiesReceived(
        payload + AutoOrchestratedOutboundConnectorOne.ID);
    mockedLoggerConnector2.expectedMessageCount(1);
    mockedLoggerConnector2.expectedBodiesReceived(
        payload + AutoOrchestratedOutboundConnectorTwo.ID);

    // act
    Exchange exchange = template.withBody(payload).to(direct("autoOrchestratedInput")).send();

    // assert
    assertThat(exchange.getException()).isNull();
    assertThat(exchange.getMessage().getBody()).isInstanceOf(String.class);
    assertThat(exchange.getMessage().getBody(String.class))
        .isIn(
            payload + AutoOrchestratedOutboundConnectorOne.ID,
            payload + AutoOrchestratedOutboundConnectorTwo.ID);
  }
}
