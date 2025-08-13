package one.x1f.sip.foundation.security.authentication.x509;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collections;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.ValidationSettings;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SIPX509FileValidatorTest {

  private static final String X_509_TEST_GOOD_ACL = "x509-test-good.acl";
  private static final String X_509_TEST_BAD_ACL = "x509-test-bad.acl";

  @Test
  void WHEN_ctor_WITH_badFileConfig_THEN_exception() {
    // act + assert
    assertThatExceptionOfType(SIPFrameworkException.class)
        .isThrownBy(() -> getSipX509FileValidator(X_509_TEST_BAD_ACL));
  }

  @Test
  void WHEN_isValid_WITH_validUser_THEN_true() {
    // arrange
    SIPX509FileValidator subject = getSipX509FileValidator(X_509_TEST_GOOD_ACL);

    SIPX509AuthenticationToken authToken =
        new SIPX509AuthenticationToken(
            "CN=Full Name, EMAILADDRESS=name@domain.de, O=[*], C=DE", false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token should have been successfully validated, but it failed")
        .isTrue();
  }

  @Test
  void WHEN_isValid_WITH_validUserWithWildcard_THEN_true() {
    // arrange
    SIPX509FileValidator subject = getSipX509FileValidator(X_509_TEST_GOOD_ACL);

    SIPX509AuthenticationToken authToken =
        new SIPX509AuthenticationToken(
            "CN=Full Name, EMAILADDRESS=name@domain.de, O=whateverThisShouldBeIgnored, C=DE",
            false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token should have been successfully validated, but it failed")
        .isTrue();
  }

  @Test
  void WHEN_isValid_WITH_invalidUser_THEN_false() {
    // arrange
    SIPX509FileValidator subject = getSipX509FileValidator(X_509_TEST_GOOD_ACL);

    SIPX509AuthenticationToken authToken =
        new SIPX509AuthenticationToken(
            "CN=Full Name, EMAILADDRESS=unknown@unknown.de, O=[*], C=DE", false);

    // act
    boolean result = subject.isValid(authToken);

    // assert
    assertThat(result)
        .withFailMessage("Auth token validation should have failed, but it succeeded")
        .isFalse();
  }

  private SIPX509FileValidator getSipX509FileValidator(String fileName) {
    AuthProviderSettings authProvSettings = new AuthProviderSettings();
    authProvSettings.setClassname(SIPX509AuthenticationProvider.class);
    authProvSettings.setValidation(
        new ValidationSettings(SIPX509FileValidator.class, new ClassPathResource(fileName)));
    SecurityConfigProperties config = new SecurityConfigProperties();
    config.setAuthProviders(Collections.singletonList(authProvSettings));

    return new SIPX509FileValidator(config);
  }
}
