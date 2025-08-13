package one.x1f.sip.foundation.core.apps.declarative.connectorextensions;

import java.util.function.Function;
import one.x1f.sip.foundation.core.apps.declarative.ConnectorProcessorExtensionsAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.ExecuteBefore;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.ResponseExtension;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorExtension;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ResponseExtension(ConnectorProcessorExtensionsAdapter.RestParamMappingOutboundConnector.class)
@ExecuteBefore(extensionName = "attachFourthString")
public class ExternalConnectorExtension implements ConnectorExtension {

  public static final String BEAN_NAME = "ConnectorExtensionBean";

  @Override
  public void accept(RouteDefinition routeDefinition) {
    routeDefinition
        .log(LoggingLevel.WARN, "Routing to additional to()-Bean")
        .to(StaticEndpointBuilders.bean(BEAN_NAME));
  }

  @Bean(BEAN_NAME)
  public Function<String, String> attachViaToBean() {
    return body -> body += " extBean";
  }
}
