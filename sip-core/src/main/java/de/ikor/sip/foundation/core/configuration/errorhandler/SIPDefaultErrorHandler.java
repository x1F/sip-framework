package de.ikor.sip.foundation.core.configuration.errorhandler;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Default logging onException definition */
@AutoConfiguration
@ConditionalOnProperty(value = "sip.core.errorhandler.default.enabled", havingValue = "true")
public class SIPDefaultErrorHandler extends RouteConfigurationBuilder {
  @Override
  public void configuration() throws Exception {
    routeConfiguration()
        .onException(Exception.class)
        .log(LoggingLevel.ERROR, "exception")
        .id("default-sip-error-handler");
  }
}
