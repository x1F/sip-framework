package one.x1f.sip.foundation.security.authentication.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.util.Collections;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.ValidationSettings;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SIPBasicAuthFileValidatorTest {

  private static final String BASIC_AUTH_USERS_BAD_JSON = "basic-auth-users-bad.json";
  private static final String BASIC_AUTH_USERS_GOOD_JSON = "basic-auth-users-good.json";
  private static final String USER = "user";
  private static final String PASSWORD = "password";

  @Test
  void WHEN_ctor_WITH_badFileConfig_THEN_exception() {
    // act + assert
    assertThatExceptionOfType(SIPFrameworkException.class)
        .isThrownBy(() -> getSipBasicAuthFileValidator(BASIC_AUTH_USERS_BAD_JSON));
  }

  @Test
  void WHEN_isValid_WITH_validUserAndPassword_THEN_true() throws Exception {
    // arrange
    SIPBasicAuthFileValidator subject = getSipBasicAuthFileValidator(BASIC_AUTH_USERS_GOOD_JSON);

    SIPBasicAuthAuthenticationToken authToken =
        new SIPBasicAuthAuthenticationToken(USER, PASSWORD, false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token should have been successfully validated, but it failed")
        .isTrue();
  }

  @Test
  void WHEN_isValid_WITH_invalidUser_THEN_false() throws Exception {
    // arrange
    SIPBasicAuthFileValidator subject = getSipBasicAuthFileValidator(BASIC_AUTH_USERS_GOOD_JSON);

    SIPBasicAuthAuthenticationToken authToken =
        new SIPBasicAuthAuthenticationToken("baduser", PASSWORD, false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token validation should have failed, but it succeeded")
        .isFalse();
  }

  @Test
  void WHEN_isValid_WITH_invalidPassword_THEN_false() throws Exception {
    // arrange
    SIPBasicAuthFileValidator subject = getSipBasicAuthFileValidator(BASIC_AUTH_USERS_GOOD_JSON);

    SIPBasicAuthAuthenticationToken authToken =
        new SIPBasicAuthAuthenticationToken(USER, "badpassword", false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token validation should have failed, but it succeeded")
        .isFalse();
  }

  private SIPBasicAuthFileValidator getSipBasicAuthFileValidator(String fileName)
      throws IOException {
    AuthProviderSettings authProvSettings = new AuthProviderSettings();
    authProvSettings.setClassname(SIPBasicAuthAuthenticationProvider.class);
    authProvSettings.setValidation(
        new ValidationSettings(SIPBasicAuthFileValidator.class, new ClassPathResource(fileName)));
    SecurityConfigProperties config = new SecurityConfigProperties();
    config.setAuthProviders(Collections.singletonList(authProvSettings));

    return new SIPBasicAuthFileValidator(config);
  }
}
