package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker;

import java.util.Optional;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

/** Route invoker interface */
public interface RouteInvoker {

  String TEST_NAME_HEADER = "_SipTestName";

  /**
   * Sends request to route
   *
   * @param exchange {@link Exchange}
   * @return {@link Optional} result of route execution, empty when invoking no reply components.
   */
  Optional<Exchange> invoke(Exchange exchange);

  /**
   * Match Endpoint with proper RouteInvoker
   *
   * @param endpoint {@link Endpoint}
   * @return boolean true when matching
   */
  boolean isApplicable(Endpoint endpoint);

  /**
   * Check if component from RouteInvoker is for suspending
   *
   * @param endpoint {@link Endpoint}
   * @return boolean true when matching
   */
  default boolean shouldSuspend(Endpoint endpoint) {
    return false;
  }
}
