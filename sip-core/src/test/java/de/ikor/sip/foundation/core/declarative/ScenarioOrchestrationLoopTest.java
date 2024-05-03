package de.ikor.sip.foundation.core.declarative;

import static de.ikor.sip.foundation.core.apps.declarative.ScenarioOrchestrationLoopAdapter.CONDITION_VALUE;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.ScenarioOrchestrationLoopAdapter;
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
@SpringBootTest(classes = {ScenarioOrchestrationLoopAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:*")
@DirtiesContext
class ScenarioOrchestrationLoopTest {
  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:AfterLoopOutboundConnector")
  private MockEndpoint mockedAfterLoopOutboundConnector;

  @EndpointInject("mock:log:InsideLoopOutboundConnector")
  private MockEndpoint mockedInsideLoopOutboundConnector;

  @BeforeEach
  void setup() {
    mockedAfterLoopOutboundConnector.reset();
    mockedInsideLoopOutboundConnector.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedAfterLoopOutboundConnector.assertIsSatisfied();
    mockedInsideLoopOutboundConnector.assertIsSatisfied();
  }

  @Test
  void WHEN_callingNonProcessOrchestratorInboundConnectors_THEN_TheyReceiveAMessage() {
    // arrange
    mockedAfterLoopOutboundConnector.expectedBodiesReceivedInAnyOrder(
        "CallLoopResponse[name=MyPartnerCode]");
    mockedInsideLoopOutboundConnector.expectedHeaderValuesReceivedInAnyOrder(
        CONDITION_VALUE, "", "a", "aa", "aaa", "aaaa");

    // act
    Exchange exchangeFirstConnector =
        template.withBody("MyPartner").to(direct("CallLoopInboundConnector")).send();
    ScenarioOrchestrationLoopAdapter.FinalResponse responseFirstConnector =
        exchangeFirstConnector
            .getMessage()
            .getBody(ScenarioOrchestrationLoopAdapter.FinalResponse.class);

    // assert
    assertThat(exchangeFirstConnector.getException()).isNull();
    assertThat(responseFirstConnector.name()).isEqualTo("MyPartnerCode");
    assertThat(responseFirstConnector.condition()).isEqualTo("aaaaa");
  }
}
