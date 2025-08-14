package one.x1f.sip.foundation.security.authentication.common.validators;

import one.x1f.sip.foundation.security.authentication.SIPAuthenticationToken;

/**
 * Functional interface for all token validators
 *
 * @author thomas.stieglmaier
 * @param <T> the type of token to be validated
 */
@FunctionalInterface
public interface SIPTokenValidator<T extends SIPAuthenticationToken<T>> {

  /**
   * Checks if a given token is valid
   *
   * @param token the token to be checked
   * @return indicates if the token is valid or not
   */
  boolean isValid(T token);
}
