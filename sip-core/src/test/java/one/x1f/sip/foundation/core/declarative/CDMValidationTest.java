package one.x1f.sip.foundation.core.declarative;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.Assertions.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.CDMValidationAdapter;
import one.x1f.sip.foundation.core.apps.declarative.CDMValidationAdapter.CDMRequest;
import one.x1f.sip.foundation.core.apps.declarative.CDMValidationAdapter.CDMResponse;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(classes = {CDMValidationAdapter.class})
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class CDMValidationTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private FluentProducerTemplate template;

  @BeforeEach
  void setup() {
    mockedLogger.reset();
  }

  @Test
  void when_CDMIsValid_then_NoExceptionOccurs() throws InterruptedException {
    CDMRequest cdmRequest = new CDMRequest(1000);
    CDMResponse cdmResponse = new CDMResponse("ID: 1000");
    mockedLogger.expectedMessageCount(1);
    mockedLogger.expectedBodiesReceived(cdmResponse);

    template.withBody(cdmRequest).to(direct("cdm-validator")).send();

    mockedLogger.assertIsSatisfied();
  }

  @Test
  void when_CDMRequestIsNotValid_then_ExceptionOccurs() {
    Exchange target = template.withBody("String").to(direct("cdm-validator")).send();

    assertThat(target.getException())
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(
            "Data type mismatch in Connector 'in-CDMValidation-SIP1'. Message Type was 'java.lang.String', but the integration scenario 'CDMValidation' requires Type '%s'",
            CDMRequest.class.getName());
  }

  @Test
  void when_CDMResponseIsNotValid_then_ExceptionOccurs() {
    Exchange target = template.withBody(new CDMRequest(1001)).to(direct("cdm-validator")).send();

    assertThat(target.getException())
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(
            "Data type mismatch in Connector 'out-CDMValidation-SIP2'. Message Type was 'java.lang.String', but the integration scenario 'CDMValidation' requires Type '%s'",
            CDMResponse.class.getName());
  }
}
