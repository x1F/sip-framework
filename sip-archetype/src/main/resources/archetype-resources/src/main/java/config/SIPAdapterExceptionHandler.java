package ${package}.config;

import de.ikor.sip.foundation.core.util.exception.SIPAdapterException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * Implementation of Apache Camel's {@link RouteConfigurationBuilder} which handles {@link SIPAdapterException}
 */
@Configuration
public class SIPAdapterExceptionHandler extends RouteConfigurationBuilder {
    @Override
    public void configuration() throws Exception {
        routeConfiguration()
                .onException(SIPAdapterException.class, IllegalArgumentException.class)
                .process(exchange -> {
                    String message = exchange
                            .getProperty(Exchange.EXCEPTION_CAUGHT, SIPAdapterException.class)
                            .getMessage();
                    exchange.getMessage().setBody(message);
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                })
                .handled(true);
    }
}