package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions;

import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;

/** Exception for impossible invoking first processor in tested route */
public class RouteInvokerException extends SIPFrameworkException {

  public RouteInvokerException(String routeInvokerName) {
    super(String.format("%s could not invoke route under test", routeInvokerName));
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
