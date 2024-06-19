package de.ikor.sip.foundation.core.declarative.configuration;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * Builds Apache Camel route configurations defined with {@link DeclarativeConfigurationDefinition}
 */
@Configuration
@RequiredArgsConstructor
public class DeclarativeConfigurationBuilder extends RouteConfigurationBuilder {

  private final List<DeclarativeConfigurationDefinition> definitions;

  @Override
  public void configuration() throws Exception {
    for (var def : definitions) {
      var config = routeConfiguration(ClassUtils.getShortName(def.getClass()));
      def.define(config);
    }
  }
}
