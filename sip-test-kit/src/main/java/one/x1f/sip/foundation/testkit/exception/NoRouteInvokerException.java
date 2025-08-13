package one.x1f.sip.foundation.testkit.exception;

import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvokerFactory;

/** Exception for non-existent {@link RouteInvoker} in {@link RouteInvokerFactory} */
public class NoRouteInvokerException extends Exception {

  public NoRouteInvokerException(String routeId) {
    super(String.format("No Route Invoker could be found for route id: %s", routeId));
  }

  /**
   * Method for hiding stack trace in console
   *
   * @return Throwable
   */
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
