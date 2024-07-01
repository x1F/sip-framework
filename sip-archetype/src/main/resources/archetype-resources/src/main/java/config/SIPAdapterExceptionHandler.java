package ${package}.config;

import de.ikor.sip.foundation.core.declarative.configuration.ConfigurationDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPAdapterException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.RouteConfigurationDefinition;
import org.springframework.context.annotation.Configuration;

/**
 * Implementation of Apache Camel's {@link RouteConfigurationBuilder} which handles
 * {@link SIPAdapterException} and {@link IllegalArgumentException}
 */
@Configuration
public class SIPAdapterExceptionHandler implements ConfigurationDefinition {

    @Override
    public OutputDefinition define(RouteConfigurationDefinition routeConfigurationDefinition) {
        return routeConfigurationDefinition
                .onException(SIPAdapterException.class, IllegalArgumentException.class)
                .process(exchange -> {
                    String message = exchange
                            .getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                            .getMessage();
                    exchange.getMessage().setBody(message);
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                })
                .handled(true);
    }
}