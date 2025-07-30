package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;

@ToString
@Slf4j
public class MethodBasedConnectorExtension implements ConnectorExtension {

  @Getter protected final ConnectorDefinition connector;
  @Getter protected final Method extensionMethod;

  public MethodBasedConnectorExtension(
      final ConnectorDefinition connector, final Method extensionMethod) {
    this.connector = connector;
    this.extensionMethod = extensionMethod;
  }

  @Override
  public String getExtensionName() {
    return extensionMethod.getName();
  }

  @Override
  @SneakyThrows
  public void accept(RouteDefinition routeDefinition) {
    try {
      extensionMethod.invoke(connector, routeDefinition);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Exception exception) throw exception;
      throw SIPFrameworkException.init(
          e.getCause(),
          "An error occurred while running connector extension method '%s' in connector-class %s: %s",
          extensionMethod.getName(),
          connector.getClass().getName(),
          e.getCause().getMessage());
    } catch (IllegalAccessException e) {
      throw SIPFrameworkException.init(
          e,
          "Failed to run connector extension method '%s' in connector-class %s",
          extensionMethod.getName(),
          connector.getClass().getName());
    }
  }
}
