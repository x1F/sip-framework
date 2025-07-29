package de.ikor.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.ProcessOrchestrationSplitAdapter;
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
@SpringBootTest(classes = {ProcessOrchestrationSplitAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:*")
@DirtiesContext
class ProcessOrchestrationSplitTest {

  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:AfterSplitOutboundConnector")
  private MockEndpoint mockedAfterSplitOutboundConnector;

  @EndpointInject("mock:log:InsideSplitOutboundConnector")
  private MockEndpoint mockedInsideSplitOutboundConnector;

  @BeforeEach
  void setup() {
    mockedAfterSplitOutboundConnector.reset();
    mockedInsideSplitOutboundConnector.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedInsideSplitOutboundConnector.assertIsSatisfied();
  }

  @Test
  void WHEN_callingNonProcessOrchestratorInboundConnectors_THEN_TheyReceiveAMessage() {
    // arrange
    mockedInsideSplitOutboundConnector.expectedBodiesReceivedInAnyOrder("John", "Jane");

    // act
    Exchange exchangeFirstConnector =
        template.withBody("").to(direct("CallSplitInboundConnector")).send();
    ProcessOrchestrationSplitAdapter.CallSplitResponse response =
        exchangeFirstConnector
            .getMessage()
            .getBody(ProcessOrchestrationSplitAdapter.CallSplitResponse.class);

    // assert
    assertThat(exchangeFirstConnector.getException()).isNull();
    assertThat(response.updatedNames().size()).isEqualTo(3);
    assertThat(response.updatedNames().toString()).contains("John Doe", "Jane Doe", "Jon Doe");
  }
}
