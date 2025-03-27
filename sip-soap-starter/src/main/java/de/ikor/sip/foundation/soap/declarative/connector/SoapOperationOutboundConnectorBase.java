package de.ikor.sip.foundation.soap.declarative.connector;

import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.model.MarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import de.ikor.sip.foundation.soap.utils.OutboundSOAPMarshallerDefinition;
import de.ikor.sip.foundation.soap.utils.SOAPEndpointBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.util.Optional;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for SOAP outbound connectors.
 *
 * <p>This class allows to bind <em>one</em> operation of a SOAP service interface to the
 * integration scenario of the connector.
 *
 * <p>The base implementation will bind JAXB marshallers automatically.
 *
 * <p>In standard cases, adapter developers need only to implement {@link #getServiceAddress()} and
 * {@link #getServiceOperationName()} optionally {@link #getServiceInterfaceClass()}
 *
 * @param <T> Service interface type
 */
public abstract class SoapOperationOutboundConnectorBase<T> extends GenericOutboundConnectorBase {

  private Class<T> serviceClass;
  private JaxbDataFormat dataFormat;

  @SuppressWarnings("unchecked")
  protected SoapOperationOutboundConnectorBase() {
    try {
      this.serviceClass =
          (Class<T>)
              DeclarativeReflectionUtils.getClassFromGeneric(
                  getClass(), SoapOperationOutboundConnectorBase.class);
      this.dataFormat = getDataFormat();
    } catch (Exception e) {
      this.serviceClass = null;
    }
  }

  @Override
  protected EndpointProducerBuilder defineOutgoingEndpoint() {

    return SOAPEndpointBuilder.generateCXFEndpoint(
        getId(),
        getApplicationContext().getBeansOfType(CxfEndpoint.class),
        getServiceInterfaceClass().getSimpleName(),
        getServiceInterfaceClass().getName(),
        getServiceAddress(),
        getSOAPMessageFormat());
  }

  @Override
  protected Optional<MarshallerDefinition> defineRequestMarshalling() {
    return Optional.of(
        OutboundSOAPMarshallerDefinition.forDataFormatWithOperationAndAddress(
            dataFormat, getServiceOperationName(), getServiceAddress()));
  }

  @Override
  protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
    return getJaxbContextPathForResponseModel()
        .map(contextPath -> UnmarshallerDefinition.forDataFormat(dataFormat));
  }

  /**
   * Returns the JAXB context path for the request model.
   *
   * @return Context path - defaults to the package name of the request model class
   */
  protected String getJaxbContextPathForRequestModel() {
    return getRequestModelClass().getPackageName();
  }

  /**
   * Returns the JAXB context path for the response model.
   *
   * @return Context path - defaults to the package name of the response model class
   */
  protected Optional<String> getJaxbContextPathForResponseModel() {
    return getResponseModelClass().map(Class::getPackageName);
  }

  /**
   * Returns the service interface class
   *
   * <p>This is typically the service interface generated from a WSDL resource
   *
   * @return Service interface class
   */
  public Class<T> getServiceInterfaceClass() {
    if (serviceClass == null) {
      throw SIPFrameworkInitializationException.init(
          "SIP Framework can't infer Service class of %s Outbound SOAP Connector. Please @Override getServiceInterfaceClass() method.",
          getClass().getName());
    }
    return serviceClass;
  }

  /**
   * Returns the name of the operation that should be bound to the connector/integration scenario
   *
   * @return Service operation name
   */
  public abstract String getServiceOperationName();

  protected String getServiceAddress() {
    return StringUtils.EMPTY;
  }

  protected DataFormat getSOAPMessageFormat() {
    return DataFormat.PAYLOAD;
  }

  protected JaxbDataFormat getDataFormat() throws JAXBException {
    return new SIPJaxbDataFormat(JAXBContext.newInstance(getJaxbContextPathForRequestModel()));
  }
}
