package one.x1f.sip.foundation.security.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.authentication.CompositeAuthenticationFilter;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.common.extractors.TokenExtractors;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Central place of all configs related for spring-security settings regarding the sip
 * authentication features
 */
@Configuration
public class SecurityConfig {

  private final List<SIPAuthenticationProvider<?>> authProviders;

  private final SecurityConfigProperties config;

  private final TokenExtractors tokenExtractors;

  /**
   * Autowired constructor for creating SIP Security Configuration
   *
   * @param authProviders (optional) list of auth providers defined in the config
   * @param config SIP Security config
   * @param tokenExtractors (optional) registered token extractors filled by authProviders
   */
  @Autowired
  public SecurityConfig(
      Optional<List<SIPAuthenticationProvider<?>>> authProviders,
      SecurityConfigProperties config,
      Optional<TokenExtractors> tokenExtractors) {
    this.authProviders = authProviders.orElse(Collections.emptyList());
    this.config = config;
    this.tokenExtractors = tokenExtractors.orElse(null);
  }

  /**
   * Register custom authenticationManager as a @Bean
   *
   * @return Spring's Authentication Manager
   * @throws SIPFrameworkException due to misconfiguration
   */
  @Bean
  public AuthenticationManager authenticationManagerBean() throws SIPFrameworkException {
    List<Class<?>> autowiredAuthProviders =
        List.copyOf(authProviders.stream().map(Object::getClass).toList());

    List<Class<?>> providersUnavailableAtRuntime =
        List.copyOf(
            config.getAuthProviders().stream()
                .map(AuthProviderSettings::getClassname)
                .filter(a -> !autowiredAuthProviders.contains(a))
                .toList());

    if (!providersUnavailableAtRuntime.isEmpty()) {
      throw SIPFrameworkException.init(
          "Some providers declared in the config are not available in runtime: %s",
          providersUnavailableAtRuntime);
    }

    if (configHasDuplicateAuthProviders()) {
      throw new SIPFrameworkException(
          "Each auth provider may only be configured once, duplicates are not allowed");
    }
    if (authProviders.isEmpty()) {
      return new ProviderManager(new NullAuthenticationProvider());
    }
    // Register all auth providers that exist in the config
    List<AuthenticationProvider> authenticationProviders = new ArrayList<>();
    authProviders.stream()
        .filter(
            a ->
                config.getAuthProviders().stream()
                    .map(AuthProviderSettings::getClassname)
                    .anyMatch(n -> n.equals(a.getClass())))
        .forEach(authenticationProviders::add);
    return new ProviderManager(authenticationProviders);
  }

  private boolean configHasDuplicateAuthProviders() {
    return config.getAuthProviders().size()
        > config.getAuthProviders().stream()
            .map(AuthProviderSettings::getClassname)
            .distinct()
            .count();
  }

  /**
   * @param http Spring provided http security object used for configuring rules
   * @return Spring's SecurityFilterChain
   * @throws Exception if security isn't properly configured
   */
  @Bean
  public SecurityFilterChain sipDefaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(config.getMatcherPatterns());
    // disable sessions completely
    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    // add our composite authentication Filter for all requests (besides the ones ignored separately
    // in the WebSecurity configure method
    if (config.isDisableCsrf()) {
      http.csrf(AbstractHttpConfigurer::disable);
    }

    http.addFilterAt(
            new CompositeAuthenticationFilter(tokenExtractors, config, authenticationManagerBean()),
            BasicAuthenticationFilter.class)
        .authorizeHttpRequests(
            authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.anyRequest().authenticated());

    return new OrderedSecurityFilterChain(config.getOrder(), http.build());
  }

  /**
   * Set globally ignored endpoints from config
   *
   * @return Spring WebSecurityCustomizer
   */
  @Bean
  public WebSecurityCustomizer sipDefaultWebSecurityCustomizer() {
    return (web -> {
      final WebSecurity.IgnoredRequestConfigurer ignoredRequestConfigurer = web.ignoring();
      config
          .getIgnoredEndpoints()
          .forEach(
              endpoint ->
                  ignoredRequestConfigurer.requestMatchers(
                      AntPathRequestMatcher.antMatcher(endpoint)));
    });
  }
}
