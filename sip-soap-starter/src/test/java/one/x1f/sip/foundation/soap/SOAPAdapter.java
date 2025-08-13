package one.x1f.sip.foundation.soap;

import com.example.customerservice.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.UseRequestModelMapper;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import one.x1f.sip.foundation.soap.declarative.connector.SoapOperationInboundConnectorBase;
import one.x1f.sip.foundation.soap.declarative.connector.SoapOperationOutboundConnectorBase;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.env.Environment;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class SOAPAdapter {

  private static final String SOAP_ADDRESS = "customAddress";

  @Bean("CustomerService")
  public CxfEndpoint createCustomerServiceEndpoint() throws ClassNotFoundException {
    CxfEndpoint serviceEndpoint = new CxfEndpoint();
    serviceEndpoint.setAddress(SOAP_ADDRESS);
    serviceEndpoint.setDataFormat(DataFormat.PAYLOAD);
    return serviceEndpoint;
  }

  @IntegrationScenario(
      scenarioId = GetCustomerByNameFrontEnd.ID,
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class)
  public static class GetCustomerByNameFrontEnd extends IntegrationScenarioBase {

    public static final String ID = "GetCustomerByNameFrontEnd";
  }

  public static class FrontEndSystemRequestMapper
      implements ModelMapper<String, GetCustomersByName> {

    @Override
    public GetCustomersByName mapToTargetModel(String sourceRequest) {
      GetCustomersByName request = new GetCustomersByName();
      request.setName(sourceRequest);
      return request;
    }
  }

  @InboundConnector(
      connectorGroup = "front-end",
      integrationScenario = GetCustomerByNameFrontEnd.ID,
      requestModel = String.class,
      responseModel = GetCustomersByNameResponse.class)
  @UseRequestModelMapper(FrontEndSystemRequestMapper.class)
  public static class GetCustomerByNameFrontEndProvider extends GenericInboundConnectorBase {

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("triggerAdapter-getCustomerByName");
    }
  }

  @OutboundConnector(
      connectorGroup = "soap",
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class,
      integrationScenario = GetCustomerByNameFrontEnd.ID)
  public static class GetCustomersByNameOutboundSOAPConnector
      extends SoapOperationOutboundConnectorBase<CustomerService> {

    @Autowired Environment environment;

    @Override
    public String getServiceOperationName() {
      return "getCustomersByName";
    }

    @Override
    public String getServiceAddress() {
      return String.format(
          "http://localhost:%s/soap-ws/%s",
          environment.getProperty("local.server.port"), SOAP_ADDRESS);
    }
  }

  @IntegrationScenario(
      scenarioId = GetCustomerByNameBackEnd.ID,
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class)
  public static class GetCustomerByNameBackEnd extends IntegrationScenarioBase {

    public static final String ID = "GetCustomerByNameBackend";
  }

  @InboundConnector(
      connectorGroup = "soap",
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class,
      integrationScenario = GetCustomerByNameBackEnd.ID)
  public static class GetCustomersByNameConnector
      extends SoapOperationInboundConnectorBase<CustomerService> {

    @Override
    public String getServiceOperationName() {
      return "getCustomersByName";
    }
  }

  @InboundConnector(
      connectorId = "non-existing-operation-connector",
      connectorGroup = "soap",
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class,
      integrationScenario = GetCustomerByNameBackEnd.ID)
  public static class NonExistingOperationConnector
      extends SoapOperationInboundConnectorBase<CustomerService> {

    @Override
    public String getServiceOperationName() {
      return "nonExistingOperation";
    }
  }

  @OutboundConnector(
      integrationScenario = GetCustomerByNameBackEnd.ID,
      requestModel = GetCustomersByName.class,
      responseModel = GetCustomersByNameResponse.class,
      connectorGroup = "mock")
  public static class GetCustomerByNameMockResponseConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.bean("CustomerMockResponse");
    }

    @Bean("CustomerMockResponse")
    public Supplier<GetCustomersByNameResponse> buildMockResponse() {
      final var response = new GetCustomersByNameResponse();
      var customer = new Customer();
      customer.setName("Max Mustermann");
      customer.setType(CustomerType.PRIVATE);
      customer.setNumOrders(ThreadLocalRandom.current().nextInt(0, 22));
      customer.setRevenue(2391.4);
      response.getReturn().add(customer);
      return () -> response;
    }
  }
}
