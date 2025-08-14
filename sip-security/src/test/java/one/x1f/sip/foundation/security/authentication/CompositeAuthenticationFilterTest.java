package one.x1f.sip.foundation.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.util.Collections;
import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationToken;
import one.x1f.sip.foundation.security.authentication.common.extractors.TokenExtractors;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CompositeAuthenticationFilterTest {

  @Mock private TokenExtractors tokenExtractors;

  @Mock private AuthenticationManager authManager;

  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();

  private SecurityConfigProperties config;
  private CompositeAuthenticationFilter subject;
  private SIPBasicAuthAuthenticationToken token =
      new SIPBasicAuthAuthenticationToken("user", "password", true);

  @BeforeEach
  void beforeEach() {
    AuthProviderSettings authProviderSettings = new AuthProviderSettings();
    authProviderSettings.setClassname(SIPBasicAuthAuthenticationProvider.class);
    authProviderSettings.setIgnoredEndpoints(Collections.singletonList("/ignored"));
    config = new SecurityConfigProperties();
    config.setAuthProviders(Collections.singletonList(authProviderSettings));

    subject = new CompositeAuthenticationFilter(tokenExtractors, config, authManager);
    lenient()
        .when(tokenExtractors.extractTokenFor(eq(SIPBasicAuthAuthenticationProvider.class), any()))
        .thenReturn(token);
  }

  @Test
  void WHEN_doFilterInternal_WITH_badCredentials_THEN_401() throws Exception {
    // arrange
    when(authManager.authenticate(token)).thenThrow(BadCredentialsException.class);

    // act
    subject.doFilter(request, response, null);

    // assert
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void WHEN_doFilterInternal_WITH_incompleteCredentials_THEN_403() throws Exception {
    // arrange
    when(authManager.authenticate(token)).thenThrow(InsufficientAuthenticationException.class);

    // act
    subject.doFilter(request, response, null);

    // assert
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  void WHEN_doFilterInternal_WITH_serverError_THEN_500() throws Exception {
    // arrange
    when(authManager.authenticate(token)).thenThrow(IllegalStateException.class);

    // act
    subject.doFilter(request, response, null);

    // assert
    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  void WHEN_doFilterInternal_WITH_validCredentials_THEN_everythingOk() throws Exception {
    // arrange
    when(authManager.authenticate(token)).thenReturn(token);
    FilterChain chain = mock(FilterChain.class);

    // act
    subject.doFilter(request, response, chain);

    // assert
    verify(chain).doFilter(request, response);
    Authentication contextToken = SecurityContextHolder.getContext().getAuthentication();
    assertThat(contextToken).isInstanceOf(CompositeAuthenticationToken.class);
    assertThat(((CompositeAuthenticationToken) contextToken).getAuthTokens())
        .containsExactly(token);
  }

  @Test
  void WHEN_doFilterInternal_WITH_ignoredRoute_THEN_everythingOk() throws Exception {
    // arrange
    request.setRequestURI("/ignored");
    FilterChain chain = mock(FilterChain.class);

    // act
    subject.doFilter(request, response, chain);

    // assert
    verifyNoInteractions(authManager, tokenExtractors);
    verify(chain).doFilter(request, response);
    Authentication contextToken = SecurityContextHolder.getContext().getAuthentication();
    assertThat(contextToken).isInstanceOf(CompositeAuthenticationToken.class);
    assertThat(((CompositeAuthenticationToken) contextToken).getAuthTokens()).isEmpty();
  }
}
