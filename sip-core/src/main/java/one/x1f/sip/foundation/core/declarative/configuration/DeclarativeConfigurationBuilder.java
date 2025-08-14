package one.x1f.sip.foundation.core.declarative.configuration;

import java.util.List;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.model.OnExceptionDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/** Builds Apache Camel route configurations defined with {@link ConfigurationDefinition} */
@Configuration
@RequiredArgsConstructor
public class DeclarativeConfigurationBuilder extends RouteConfigurationBuilder {

  private final List<ConfigurationDefinition> definitions;
  private final DeclarationsRegistry registry;

  @Override
  public void configuration() throws Exception {
    for (var def : definitions) {
      var config = routeConfiguration(ClassUtils.getShortName(def.getClass()));
      var processDef = def.define(config);
      if (processDef instanceof OnExceptionDefinition onExceptionDefinition) {
        registry.registerClassForOnException(
            onExceptionDefinition, ClassUtils.getUserClass(def.getClass()).getName());
        onExceptionDefinition
            .setProperty(
                DeclarativeHelper.ERROR_HANDLER,
                simple(ClassUtils.getUserClass(def.getClass()).getName()))
            .id(DeclarativeHelper.SIP_INTERNAL_SET_PROPERTY);
      }
    }
  }
}
