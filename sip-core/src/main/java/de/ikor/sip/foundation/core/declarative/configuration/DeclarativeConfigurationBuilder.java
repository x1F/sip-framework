package de.ikor.sip.foundation.core.declarative.configuration;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.model.OnExceptionDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/** Builds Apache Camel route configurations defined with {@link ConfigurationDefinition} */
@Configuration
@RequiredArgsConstructor
public class DeclarativeConfigurationBuilder extends RouteConfigurationBuilder {

  public static final String ERROR_HANDLER = "errorHandler";
  private final List<ConfigurationDefinition> definitions;

  @Override
  public void configuration() throws Exception {
    for (var def : definitions) {
      var config = routeConfiguration(ClassUtils.getShortName(def.getClass()));
      var processDef = def.define(config);
      if (processDef instanceof OnExceptionDefinition onExceptionDefinition) {
        onExceptionDefinition.setProperty(
            ERROR_HANDLER, simple(ClassUtils.getUserClass(def.getClass()).getName()));
      }
    }
  }
}
