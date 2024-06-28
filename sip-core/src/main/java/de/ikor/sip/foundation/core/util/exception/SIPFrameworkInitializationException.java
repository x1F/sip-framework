package de.ikor.sip.foundation.core.util.exception;

import lombok.experimental.StandardException;

/** Exception class for exception that are thrown by the framework during initialization phase */
@StandardException
public class SIPFrameworkInitializationException extends SIPFrameworkException {

  /**
   * Static method for creating exception with provided message pattern and message arguments.
   *
   * @param messagePattern exception message in form of a string pattern
   * @param args arguments for message pattern
   * @return initialized SIPFrameworkInitializationException
   */
  public static SIPFrameworkInitializationException init(String messagePattern, Object... args) {
    return new SIPFrameworkInitializationException(String.format(messagePattern, args));
  }

  /**
   * Static method for creating exception with provided message pattern and message arguments.
   *
   * @param cause exception cause
   * @param messagePattern exception message in form of a string pattern
   * @param args arguments for message pattern
   * @return initialized SIPFrameworkInitializationException
   */
  public static SIPFrameworkInitializationException init(
      Throwable cause, String messagePattern, Object... args) {
    return new SIPFrameworkInitializationException(String.format(messagePattern, args), cause);
  }

  /**
   * Throws the specified initialization exception if the given <code>expression</code> is true
   *
   * @param expression expression result
   * @param messagePattern exception message in form of a string pattern
   * @param args arguments for message pattern
   */
  public static void throwIf(boolean expression, String messagePattern, Object... args) {
    if (expression) {
      throw init(messagePattern, args);
    }
  }
}
