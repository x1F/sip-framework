package de.ikor.sip.foundation.soap.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.example.customerservice.CustomerService;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.Collections;
import java.util.Map;
import org.apache.camel.builder.endpoint.dsl.CxfEndpointBuilderFactory.CxfEndpointBuilder;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.junit.jupiter.api.Test;

class SOAPEndpointBuilderTest {

  @Test
  void WHEN_beanNotExistsInRegistry_THEN_CamelEndpointIsCreated() {

    // arrange
    Map<String, CxfEndpoint> cxfBeans = Collections.emptyMap();

    // act
    CxfEndpointBuilder cxfEndpointBuilder =
        SOAPEndpointBuilder.generateCXFEndpoint(
            "connectorID",
            cxfBeans,
            "serviceClassName",
            "serviceClassQualifiedName",
            "address",
            DataFormat.PAYLOAD);

    // assert
    assertThat(cxfEndpointBuilder.getUri())
        .isEqualTo("cxf://address?dataFormat=PAYLOAD&serviceClass=serviceClassQualifiedName");
  }

  @Test
  void WHEN_beanNotExistsInRegistryAndNoAddessIsProvided_THEN_SIPFrameworkExceptionIsThrown() {

    // arrange
    Map<String, CxfEndpoint> cxfBeans = Collections.emptyMap();

    // act & assert
    assertThatThrownBy(
            () -> {
              SOAPEndpointBuilder.generateCXFEndpoint(
                  "connectorID",
                  cxfBeans,
                  "serviceClassName",
                  "serviceClassQualifiedName",
                  "",
                  DataFormat.PAYLOAD);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            String.format(
                "Connector '%s' doesn't have a defined address. Please @Override the 'getServiceAddress()' method"
                    + " or define a CXFBean with name '%s'",
                "connectorID", "serviceClassName"));
  }

  @Test
  void WHEN_beanInRegistryHasNoAddressAndAddressIsProvided_THEN_AddressIsSetOnTheBean() {

    // arrange
    CxfEndpoint cxfEndpoint = new CxfEndpoint();
    cxfEndpoint.setAddress("");
    cxfEndpoint.setServiceClass(CustomerService.class);
    Map<String, CxfEndpoint> cxfBeans = Map.of("serviceClassName", cxfEndpoint);
    String EXAMPLE_ADDRESS = "http://www.example.com/service";

    // act
    CxfEndpointBuilder cxfEndpointBuilder =
        SOAPEndpointBuilder.generateCXFEndpoint(
            "connectorID",
            cxfBeans,
            "serviceClassName",
            "serviceClassQualifiedName",
            EXAMPLE_ADDRESS,
            DataFormat.PAYLOAD);

    // assert
    assertThat(cxfEndpoint.getAddress()).isEqualTo(EXAMPLE_ADDRESS);
    assertThat(cxfEndpointBuilder.getUri()).isEqualTo("cxf://bean:serviceClassName");
  }

  @Test
  void WHEN_beanInRegistryHasNoAddressAndAddressIsNotProvided_THEN_SIPFrameworkExceptionIsThrown() {

    // arrange
    CxfEndpoint cxfEndpoint = new CxfEndpoint();
    cxfEndpoint.setAddress("");
    cxfEndpoint.setServiceClass(CustomerService.class);
    Map<String, CxfEndpoint> cxfBeans = Map.of("serviceClassName", cxfEndpoint);

    // act & assert
    assertThatThrownBy(
            () -> {
              SOAPEndpointBuilder.generateCXFEndpoint(
                  "connectorID",
                  cxfBeans,
                  "serviceClassName",
                  "serviceClassQualifiedName",
                  "",
                  DataFormat.PAYLOAD);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            String.format(
                "CXFEndpoint bean '%s' is defined but the SOAP address is undefined. "
                    + "Please use 'setAddress()' in the CXFEndpoint bean or @Override the 'getServiceAddress()' method in the connector '%s'",
                "serviceClassName", "connectorID"));
  }

  @Test
  void
      WHEN_beanInRegistryHasNoServiceClassAndInvalidServiceClassIsProvided_THEN_SIPFrameworkExceptionIsThrown() {

    // arrange
    CxfEndpoint cxfEndpoint = new CxfEndpoint();
    Map<String, CxfEndpoint> cxfBeans = Map.of("serviceClassName", cxfEndpoint);

    // act & assert
    assertThatThrownBy(
            () -> {
              SOAPEndpointBuilder.generateCXFEndpoint(
                  "connectorID",
                  cxfBeans,
                  "serviceClassName",
                  "invalidServiceClassQualifiedName",
                  "address",
                  DataFormat.PAYLOAD);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            String.format(
                "Service class '%s' used in the soap connector '%s' can not be found",
                "invalidServiceClassQualifiedName", "connectorID"));
  }
}
