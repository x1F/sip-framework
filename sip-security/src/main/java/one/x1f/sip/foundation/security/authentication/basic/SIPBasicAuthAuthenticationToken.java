package one.x1f.sip.foundation.security.authentication.basic;

import lombok.NonNull;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationToken;

/**
 * Container for username/password tokens in the sip authentication framework.
 *
 * @author thomas.stieglmaier
 */
public class SIPBasicAuthAuthenticationToken
    extends SIPAuthenticationToken<SIPBasicAuthAuthenticationToken> {

  private final String principal;
  private final String credentials;

  /**
   * Creates a basic auth token
   *
   * @param principal the username of the authorization header
   * @param credentials the password of the authorization header
   * @param authenticated indicates if the token is authenticated or not
   */
  public SIPBasicAuthAuthenticationToken(
      @NonNull String principal, @NonNull String credentials, boolean authenticated) {
    super(authenticated);
    this.principal = principal;
    this.credentials = credentials;
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public SIPBasicAuthAuthenticationToken withAuthenticated(boolean authenticated) {
    return new SIPBasicAuthAuthenticationToken(principal, credentials, authenticated);
  }
}
