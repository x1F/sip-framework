package one.x1f.sip.foundation.security.authentication;

import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Interface for all auth tokens used in implementations of {@link SIPAuthenticationProvider}.
 *
 * @author thomas.stieglmaier
 * @param <T> The class of the implementing token itself
 */
@AllArgsConstructor
public abstract class SIPAuthenticationToken<T extends SIPAuthenticationToken<T>>
    implements Authentication {

  private final boolean isAuthenticated;

  /**
   * Returns a new instance of the same authentication object, but with the specified value for the
   * authenticated field.
   *
   * @param authenticated indicates if the authentication object should be authenticated or not
   * @return a copy of the caller authentication object with the specific value for authentication
   *     set
   */
  public abstract T withAuthenticated(boolean authenticated);

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // for the moment we don't support more specific access control via authorities
    // so this will always return an empty set
    return Collections.emptySet();
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getDetails() {
    // for the moment we don't support more specific authentication details
    // so this will always return null
    return null;
  }

  @Override
  public final boolean isAuthenticated() {
    return isAuthenticated;
  }

  @Override
  public String getName() {
    if (getPrincipal() != null) {
      return getPrincipal().toString();
    }
    return null;
  }

  @Override
  public final void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    throw new UnsupportedOperationException(
        "An authenticated auth token has to be created separately and cannot be created via mutating this object");
  }
}
