package one.x1f.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationAdapter;
import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationAdapter.DebtResponse;
import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationAdapter.PartnerResponse;
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
@SpringBootTest(classes = {ProcessOrchestrationAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:*")
@DirtiesContext
class ProcessOrchestrationTest {

  @Autowired private FluentProducerTemplate template;

  @EndpointInject("mock:log:getPartnerByNameOutConnector")
  private MockEndpoint mockedGetPartnerByNameOutConnector;

  @EndpointInject("mock:log:getPartnerDebtByOutConnector")
  private MockEndpoint mockedGetPartnerDebtByOutConnector;

  @EndpointInject("mock:log:getPartnerDebtByNameOutLogConnector")
  private MockEndpoint mockedGetPartnerDebtByNameOutLogConnector;

  @BeforeEach
  void setup() {
    mockedGetPartnerByNameOutConnector.reset();
    mockedGetPartnerDebtByOutConnector.reset();
    mockedGetPartnerDebtByNameOutLogConnector.reset();
  }

  @AfterEach
  void assertLoggers() throws InterruptedException {
    mockedGetPartnerByNameOutConnector.assertIsSatisfied();
    mockedGetPartnerDebtByOutConnector.assertIsSatisfied();
    mockedGetPartnerDebtByNameOutLogConnector.assertIsSatisfied();
  }

  @Test
  void WHEN_callingNonProcessOrchestratorInboundConnectors_THEN_TheyReceiveAMessage() {
    // arrange
    mockedGetPartnerByNameOutConnector.expectedBodiesReceivedInAnyOrder(
        "PartnerNameRequest[name=MyPartner]");
    mockedGetPartnerDebtByOutConnector.expectedBodiesReceivedInAnyOrder(15);
    mockedGetPartnerDebtByNameOutLogConnector.expectedMessageCount(0);

    // act
    Exchange exchangeFirstConnector =
        template.withBody("MyPartner").to(direct("GetPartnerByNameInConnector")).send();
    PartnerResponse responseFirstConnector =
        exchangeFirstConnector.getMessage().getBody(PartnerResponse.class);
    Exchange exchangeSecondConnector =
        template.withBody(15).to(direct("getPartnerDebtByIdInConnector")).send();
    DebtResponse responseSecondConnector =
        exchangeSecondConnector.getMessage().getBody(DebtResponse.class);

    // assert
    assertThat(responseFirstConnector).isInstanceOf(PartnerResponse.class);
    assertThat(exchangeFirstConnector.getException()).isNull();
    assertThat(responseFirstConnector.id()).isEqualTo(1);
    assertThat(responseSecondConnector).isInstanceOf(DebtResponse.class);
    assertThat(exchangeSecondConnector.getException()).isNull();
    assertThat(responseSecondConnector.getAmount()).isEqualTo(new BigDecimal("100000.00"));
    assertThat(responseSecondConnector.getRequestedBy()).isEqualTo("Front-end");
  }

  @Test
  void WHEN_callingProcessOrchestratorInboundConnectors_THEN_ReceiveResponse() {
    // arrange
    mockedGetPartnerByNameOutConnector.expectedBodiesReceivedInAnyOrder(
        "PartnerNameRequest[name=MyOrchestratedPartner]");
    mockedGetPartnerDebtByOutConnector.expectedBodiesReceivedInAnyOrder(1);
    mockedGetPartnerDebtByNameOutLogConnector.expectedBodiesReceivedInAnyOrder(
        "PartnerNameRequest[name=MyOrchestratedPartner-LOG THIS]");

    // act
    Exchange exchange =
        template
            .withBody("MyOrchestratedPartner")
            .to(direct("GetPartnerDebtByNameInConnector"))
            .send();
    DebtResponse response = exchange.getMessage().getBody(DebtResponse.class);

    // assert
    assertThat(response).isInstanceOf(DebtResponse.class);
    assertThat(exchange.getException()).isNull();
    assertThat(response.getAmount()).isEqualTo(new BigDecimal("100000.00"));
    assertThat(response.getRequestedPartnerName()).isEqualTo("MyOrchestratedPartner");
    assertThat(response.getRequestedBy()).isEqualTo("Process Orchestrator");
  }
}
