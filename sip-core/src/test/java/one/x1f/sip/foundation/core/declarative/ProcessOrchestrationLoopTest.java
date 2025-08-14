package one.x1f.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationLoopAdapter;
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
@SpringBootTest(classes = {ProcessOrchestrationLoopAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:*")
@DirtiesContext
class ProcessOrchestrationLoopTest {

  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:AfterLoopOutboundConnector")
  private MockEndpoint mockedAfterLoopOutboundConnector;

  @EndpointInject("mock:log:InsideLoopOutboundConnector")
  private MockEndpoint mockedInsideLoopOutboundConnector;

  @EndpointInject("mock:log:LoggingOutboundConnector")
  private MockEndpoint mockedLoggingOutboundConnector;

  @BeforeEach
  void setup() {
    mockedAfterLoopOutboundConnector.reset();
    mockedInsideLoopOutboundConnector.reset();
    mockedLoggingOutboundConnector.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedAfterLoopOutboundConnector.assertIsSatisfied();
    mockedInsideLoopOutboundConnector.assertIsSatisfied();
    mockedLoggingOutboundConnector.assertIsSatisfied();
  }

  @Test
  void WHEN_callingNonProcessOrchestratorInboundConnectors_THEN_TheyReceiveAMessage() {
    // arrange
    mockedAfterLoopOutboundConnector.expectedBodiesReceivedInAnyOrder(
        "CallLoopResponse[name=MyPartnerCode]");
    mockedInsideLoopOutboundConnector.expectedHeaderValuesReceivedInAnyOrder(
        ProcessOrchestrationLoopAdapter.CONDITION_VALUE, "", "a", "aa");
    mockedLoggingOutboundConnector.expectedMessageCount(2);

    // act
    Exchange exchangeFirstConnector =
        template.withBody("MyPartner").to(direct("CallLoopInboundConnector")).send();
    ProcessOrchestrationLoopAdapter.FinalResponse responseFirstConnector =
        exchangeFirstConnector
            .getMessage()
            .getBody(ProcessOrchestrationLoopAdapter.FinalResponse.class);

    // assert
    assertThat(exchangeFirstConnector.getException()).isNull();
    assertThat(responseFirstConnector.name()).isEqualTo("MyPartnerCode");
    assertThat(responseFirstConnector.condition()).isEqualTo("aaa");
  }
}
