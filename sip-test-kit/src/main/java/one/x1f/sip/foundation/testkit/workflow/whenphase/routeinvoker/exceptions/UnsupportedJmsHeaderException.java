package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions;

import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.JmsRouteInvoker;

/** Exception for unsupported jms header in {@link JmsRouteInvoker} */
public class UnsupportedJmsHeaderException extends SIPFrameworkException {

  public UnsupportedJmsHeaderException(String header) {
    super(String.format("Camel JMS header %s is not supported ", header));
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
