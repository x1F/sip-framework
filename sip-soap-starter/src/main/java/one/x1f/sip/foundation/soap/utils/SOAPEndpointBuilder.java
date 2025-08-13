package one.x1f.sip.foundation.soap.utils;

import java.util.Map;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.builder.endpoint.dsl.CxfEndpointBuilderFactory.CxfEndpointBuilder;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.commons.lang3.StringUtils;

public class SOAPEndpointBuilder {

  private SOAPEndpointBuilder() {}

  public static CxfEndpointBuilder generateCXFEndpoint(
      String connectorID,
      Map<String, CxfEndpoint> cxfBeans,
      String serviceClassName,
      String serviceClassQualifiedName,
      String address,
      DataFormat dataFormat) {

    if (cxfBeans.containsKey(serviceClassName)) {

      CxfEndpoint cxfEndpoint = cxfBeans.get(serviceClassName);
      if (cxfEndpoint.getServiceClass() == null) {
        try {
          cxfEndpoint.setServiceClass(serviceClassQualifiedName);
        } catch (ClassNotFoundException e) {
          throw SIPFrameworkInitializationException.init(
              e,
              "Service class '%s' used in the soap connector '%s' can not be found",
              serviceClassQualifiedName,
              connectorID);
        }
      }
      if (StringUtils.isBlank(cxfEndpoint.getAddress())) {
        if (StringUtils.isBlank(address)) {
          throw SIPFrameworkInitializationException.init(
              "CXFEndpoint bean '%s' is defined but the SOAP address is undefined. Please use 'setAddress()' in the CXFEndpoint bean or @Override the 'getServiceAddress()' method in the connector '%s'",
              serviceClassName, connectorID);
        }
        cxfEndpoint.setAddress(address);
      }
      // Our route building only works with payload mode
      cxfEndpoint.setDataFormat(dataFormat);

      return StaticEndpointBuilders.cxf(String.format("bean:%s", serviceClassName));
    } else {

      if (StringUtils.isBlank(address)) {
        throw SIPFrameworkInitializationException.init(
            "Connector '%s' doesn't have a defined address. Please @Override the 'getServiceAddress()' method or define a CXFBean with name '%s'",
            connectorID, serviceClassName);
      }
      return StaticEndpointBuilders.cxf(address)
          .serviceClass(serviceClassQualifiedName)
          .dataFormat(dataFormat);
    }
  }
}
