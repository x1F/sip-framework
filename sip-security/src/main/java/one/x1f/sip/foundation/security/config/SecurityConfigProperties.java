package one.x1f.sip.foundation.security.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPAlwaysAllowValidator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Config properties for the complete sip-security authorization settings.
 *
 * @author thomas.stieglmaier
 */
@Configuration
@ConfigurationProperties("sip.security.authentication")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SecurityConfigProperties {

  private String[] matcherPatterns = {"/**"};

  private int order = 1;

  /** endpoints that should be left out of the complete authentication checks */
  private List<String> ignoredEndpoints = Collections.emptyList();

  /**
   * indicates if csrf should be disabled. Default is true, because usually sip adapters are
   * server-to-server connections and not browser-based
   */
  private boolean disableCsrf = true;

  /** The authentication providers that should be used for auth checks */
  private List<AuthProviderSettings> authProviders = Collections.emptyList();

  /**
   * Method for retrieving the proper auth provider config settings
   *
   * @param authProvider the provider for which configs should be retrieved
   * @return the settings for the provider, or null if there is no config
   */
  public AuthProviderSettings getSettingsForProvider(
      Class<? extends SIPAuthenticationProvider> authProvider) {
    return authProviders.stream()
        .filter(p -> p.getClassname().equals(authProvider))
        .findFirst()
        .orElse(null);
  }

  /**
   * Auth provider settings, containing the exact auth-provider to be used, and the respective
   * validation configs
   *
   * @author thomas.stieglmaier
   */
  @Getter
  @Setter
  @RequiredArgsConstructor
  @AllArgsConstructor
  public static class AuthProviderSettings {
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private static final Bindable<List<AuthProviderSettings>> PROVIDER_LIST =
        Bindable.listOf(AuthProviderSettings.class);

    public static final String AUTH_PROVIDERS_PROPERTY_NAME =
        "sip.security.authentication.auth-providers";

    /**
     * Bind AuthProviderSettings from PropertySource
     *
     * @param environment {@link Environment}
     * @return collection of AuthProviderSettings or null if none exist
     */
    public static Collection<AuthProviderSettings> bindFromPropertySource(Environment environment) {
      BindResult<List<AuthProviderSettings>> bindResult =
          Binder.get(environment).bind(AUTH_PROVIDERS_PROPERTY_NAME, PROVIDER_LIST);
      return bindResult.orElse(null);
    }

    /** The fully qualified classname of the current provider */
    private Class<?> classname = null;

    /**
     * endpoints that should be left out of the specific auth checks of the current provider, only
     * important if no whitelist is configured via includedEndpoints
     */
    private List<String> ignoredEndpoints = Collections.emptyList();

    /** endpoints that should be checked with the current provider, overrides blacklist settings. */
    private List<String> includedEndpoints = Collections.emptyList();

    private ValidationSettings validation;

    /**
     * Method for checking if a configured auth provider is responsible for checking authentication
     * of a specific url path
     *
     * @param urlPath the path to be checked
     * @return indicates if the auth provider should be used or not for a given path
     */
    public boolean isResponsibleFor(String urlPath) {
      if (includedEndpoints.isEmpty() && ignoredEndpoints.isEmpty()) {
        return true;
      } else if (!includedEndpoints.isEmpty()) {
        return includedEndpoints.stream().anyMatch(e -> pathMatcher.match(e, urlPath));
      }
      return ignoredEndpoints.stream().noneMatch(e -> pathMatcher.match(e, urlPath));
    }
  }

  /**
   * Validation settings for an auth provider
   *
   * @author thomas.stieglmaier
   */
  @Getter
  @Setter
  @RequiredArgsConstructor
  @AllArgsConstructor
  public static class ValidationSettings {
    /**
     * The type of validation that should be happening, by default a noop, which needs to be
     * configured differently if authentication should be actively used
     */
    private Class<?> classname = SIPAlwaysAllowValidator.class;

    /** The filepath used when validation type is set to FILE */
    private Resource filePath = null;
  }
}
