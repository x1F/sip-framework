package de.ikor.sip.foundation.core.declarative.configuration;

import java.util.List;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistry;
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
  public static final String SIP_INTERNAL_SET_PROPERTY = "sip-internal-set-property";
  private final List<ConfigurationDefinition> definitions;
  private final DeclarationsRegistry registry;

  @Override
  public void configuration() throws Exception {
    for (var def : definitions) {
      var config = routeConfiguration(ClassUtils.getShortName(def.getClass()));
      var processDef = def.define(config);
      if (processDef instanceof OnExceptionDefinition onExceptionDefinition) {
        registry.registerClassForOnException(onExceptionDefinition, ClassUtils.getUserClass(def.getClass()).getName());
        onExceptionDefinition.setProperty(
            ERROR_HANDLER, simple(ClassUtils.getUserClass(def.getClass()).getName()))
            .id(SIP_INTERNAL_SET_PROPERTY);
      }
    }
  }
}
