package de.ikor.sip.foundation.core.declarative.configuration;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.context.annotation.Configuration;

/** Builds Apache Camel route configurations defined with {@link DeclarativeConfigurationBase} */
@Configuration
@RequiredArgsConstructor
public class DeclarativeConfigurationBuilder extends RouteConfigurationBuilder {

  private final List<DeclarativeConfigurationBase> definitions;

  @Override
  public void configuration() throws Exception {
    for (var def : definitions) {
      var config = routeConfiguration(def.getName());
      def.configure().define(config);
    }
  }
}
