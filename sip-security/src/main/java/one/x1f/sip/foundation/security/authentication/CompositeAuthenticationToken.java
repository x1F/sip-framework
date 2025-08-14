package one.x1f.sip.foundation.security.authentication;

import java.util.List;
import lombok.Getter;

/**
 * Default Token set in the security context when using the sip authentication filter. It contains
 * tokens of all responsible sip authentication providers, in case this needs to be inspected in a
 * later part of the filter chain or e.g. a rest controller.
 *
 * @author thomas.stieglmaier
 */
public class CompositeAuthenticationToken
    extends SIPAuthenticationToken<CompositeAuthenticationToken> {

  @Getter private final List<SIPAuthenticationToken<?>> authTokens;

  /**
   * Creates a composite authentication token, consisting of other sip auth tokens, and an
   * aggregated isAuthenticated setting
   *
   * @param authTokens the auth tokens to combine
   */
  public CompositeAuthenticationToken(List<SIPAuthenticationToken<?>> authTokens) {
    super(
        authTokens != null
            && authTokens.stream()
                .map(SIPAuthenticationToken::isAuthenticated)
                .reduce(true, Boolean::logicalAnd));
    this.authTokens = authTokens;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  /**
   * For composite auth tokens this method does nothing, its isAuthenticated state is always
   * indicated by the contained auth tokens.
   */
  @Override
  public CompositeAuthenticationToken withAuthenticated(boolean authenticated) {
    throw new UnsupportedOperationException("Not allowed for composite auth tokens");
  }
}
