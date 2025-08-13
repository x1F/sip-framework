package one.x1f.sip.foundation.core.actuator.health;

import java.util.function.Function;

/**
 * The purpose of the {@link EndpointHealthConfigurer} is to enable application specific
 * customization of the {@link EndpointHealthRegistry}. For instance, it is possible to register
 * custom health-checks.
 */
public interface EndpointHealthConfigurer {
  /**
   * Configure {@link EndpointHealthRegistry}
   *
   * @param registry - Configures the {@link EndpointHealthRegistry}, typically by adding additional
   *     health-checking functions
   * @see EndpointHealthRegistry#register(String, Function)
   */
  void configure(EndpointHealthRegistry registry);
}
