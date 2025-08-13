package one.x1f.sip.foundation.core.actuator.health;

import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;

/**
 * Exception duplicated uri patterns (health indicator matchers) used in {@link HealthMonitorSetup}
 */
public class DuplicateUriPatternError extends SIPFrameworkException {

  /**
   * Creates new instance of DuplicateUriPatternError
   *
   * @param message message to be shown
   */
  public DuplicateUriPatternError(String message) {
    super(message);
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
