package one.x1f.sip.foundation.core.declarative.connector;

import java.util.function.Consumer;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.RequestExtension;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.ResponseExtension;
import org.apache.camel.model.RouteDefinition;

/**
 * Interface that marks extensions that can be placed within the integration flow of a connector.
 *
 * <p>Extensions of the route should be attached using the {@link #accept(Object)} method.
 *
 * @see RequestExtension
 * @see ResponseExtension
 */
@FunctionalInterface
public interface ConnectorExtension extends Consumer<RouteDefinition> {

  /**
   * Returns the name of the extension. Must be unique among all extensions of a connector.
   *
   * <p>The default implementation returns the name of the implementing class.
   *
   * @return Extension name
   */
  default String getExtensionName() {
    return getClass().getSimpleName();
  }

  /**
   * Empty {@link ConnectorExtension} implementation that is used for default assignments in
   * annotations
   */
  final class None implements ConnectorExtension {
    @Override
    public void accept(RouteDefinition routeDefinition) {
      throw new UnsupportedOperationException("Empty connector extension may not be registered");
    }
  }
}
