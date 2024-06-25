package de.ikor.sip.foundation.core.apps.declarative.connectorextensions;

import de.ikor.sip.foundation.core.apps.declarative.ConnectorProcessorExtensionsAdapter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequestProcessor(ConnectorProcessorExtensionsAdapter.RestParamMappingDirectInboundConnector.class)
public class ExternalConnectorProcessor implements ConnectorProcessor {

    public static final String ATTACHEMENT_STRING = "external-processor";

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getMessage().setBody(exchange.getMessage().getBody(String.class) + " " + ATTACHEMENT_STRING);
    }
}
