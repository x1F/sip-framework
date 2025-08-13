package one.x1f.sip.foundation.security.authentication.common.extractors;

import jakarta.servlet.http.HttpServletRequest;
import one.x1f.sip.foundation.security.authentication.CompositeAuthenticationFilter;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * The interface used for token extractors used by {@link CompositeAuthenticationFilter}, {@link
 * TokenExtractors} and {@link SIPAuthenticationProvider} implementations to extract the necessary
 * information from an http request so that the provider can check if that request is authenticated
 * or not.
 *
 * @author thomas.stieglmaier
 * @param <T> the type of the token that needs to be extracted by the provider
 */
public interface SIPTokenExtractor<T extends SIPAuthenticationToken<T>> {

  /**
   * The type of the token to be extracted
   *
   * @return the token type
   */
  Class<T> getTokenType();

  /**
   * Extracts a token of the given type from a request. Implementations of this method should throw
   * {@link BadCredentialsException} in case necessary token was not found, or had an invalid
   * format, for proper mapping of the response status code (401 Unauthorized). Other exceptions
   * will be mapped to internal server error.
   *
   * @param request the request the token should be extracted from
   * @return the extracted token
   */
  T extract(HttpServletRequest request);
}
