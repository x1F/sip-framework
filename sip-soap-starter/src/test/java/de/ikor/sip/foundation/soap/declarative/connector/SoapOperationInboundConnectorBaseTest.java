package de.ikor.sip.foundation.soap.declarative.connector;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.ikor.sip.foundation.core.declarative.annotation.InboundConnector;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.junit.jupiter.api.Test;

class SoapOperationInboundConnectorBaseTest {

  @InboundConnector(
      connectorId = "testConnector",
      integrationScenario = "testScenario",
      connectorGroup = "testGroup",
      requestModel = String.class)
  private class SOAPInboundConnector extends SoapOperationInboundConnectorBase {

    @Override
    public String getServiceOperationName() {
      return "testOperation";
    }
  }

  @Test
  void WHEN_InboundConnectorNoServiceClassCanBeInferred_THEN_SIPFrameworkExceptionIsThrown() {
    // arrange
    SoapOperationInboundConnectorBase inboundConnector = new SOAPInboundConnector();

    // act & assert
    // act & assert
    assertThatThrownBy(
            () -> {
              inboundConnector.getServiceInterfaceClass();
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            String.format(
                "SIP Framework can't infer Service class of %s Inbound SOAP Connector. Please @Override getServiceInterfaceClass() method.",
                SOAPInboundConnector.class.getName()));
  }
}
