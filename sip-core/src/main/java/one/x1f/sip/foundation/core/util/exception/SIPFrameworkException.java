package one.x1f.sip.foundation.core.util.exception;

import lombok.experimental.StandardException;

/** Base exception class for exception that are thrown by the framework */
@StandardException
public class SIPFrameworkException extends RuntimeException {

  /**
   * Static method for creating exception with provided message pattern and message arguments.
   *
   * @param messagePattern exception message in form of a string patter
   * @param args arguments for message pattern
   * @return initialized SIPFrameworkException
   */
  public static SIPFrameworkException init(String messagePattern, Object... args) {
    return new SIPFrameworkException(String.format(messagePattern, args));
  }

  /**
   * Static method for creating exception with provided message pattern and message arguments.
   *
   * @param cause exception cause
   * @param messagePattern exception message in form of a string patter
   * @param args arguments for message pattern
   * @return initialized SIPFrameworkException
   */
  public static SIPFrameworkException init(Throwable cause, String messagePattern, Object... args) {
    return new SIPFrameworkException(String.format(messagePattern, args), cause);
  }

  // Suppressing stack trace for SIP Exception, if there is a cause passed
  // to the exception, it's stack trace will still be shown.
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
