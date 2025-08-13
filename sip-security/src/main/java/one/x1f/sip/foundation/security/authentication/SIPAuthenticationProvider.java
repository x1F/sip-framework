package one.x1f.sip.foundation.security.authentication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPTokenValidator;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Base class for sip authentication providers.
 *
 * @author thomas.stieglmaier
 * @param <T> the type of {@link SIPAuthenticationToken} handled by this authentication provider
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SIPAuthenticationProvider<T extends SIPAuthenticationToken<T>>
    implements AuthenticationProvider {

  /** Additional to the generic this is again the supported token type as a class */
  @NonNull private Class<T> supportedTokenType;

  /** The token validator which should be used for validation of the authorization object */
  @NonNull protected SIPTokenValidator<T> tokenValidator;

  /**
   * The authentication method to be implmented by sip authentication providers. By default it just
   * calls the token validator, and if the token is valid, the same token is returned with the
   * <code>isAuthenticated</code> flag set to true. Otherwise the token from the argument is
   * returned as it was.
   *
   * <p>If necessary this method can be overridden to provide a more sophisticated validation logic.
   *
   * @param authentication the token which should be checked for proper authentication
   * @return a token which is either authenticated or not (can be the same as the input)
   */
  protected T validateAuthentication(T authentication) {
    if (tokenValidator.isValid(authentication)) {
      return authentication.withAuthenticated(true);
    }
    return authentication;
  }

  // the cast is safe here, this is made sure via our generics and the support methods in the
  // authentication providers
  @SuppressWarnings("unchecked")
  @Override
  public final Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    Authentication validatedToken = validateAuthentication((T) authentication);
    if (!validatedToken.isAuthenticated()) {
      if (log.isDebugEnabled()) {
        log.debug("sip.security.authtokenunsuccessful_{}", authentication.getClass());
      }
      throw new BadCredentialsException("Authentication was not successful");
    }
    return validatedToken;
  }

  @Override
  public final boolean supports(Class<?> authentication) {
    return supportedTokenType.isAssignableFrom(authentication);
  }
}
