package one.x1f.sip.foundation.security.authentication.x509;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SIPX509AuthenticationTokenTest {

  @Test
  void WHEN_ctor_WITH_nullPrincipal_THEN_IllegalArgument() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new SIPX509AuthenticationToken(null, true));
  }

  @Test
  void WHEN_ctor_WITH_validParams_THEN_correctValueslReturned() throws Exception {
    // arrange
    String expectedPrincipal = UUID.randomUUID().toString();

    // act
    SIPX509AuthenticationToken subject = new SIPX509AuthenticationToken(expectedPrincipal, false);

    // assert
    assertCtorCorrectValues(expectedPrincipal, subject);
    assertThat(subject.isAuthenticated()).isFalse();
  }

  @Test
  void WHEN_withAuthenticated_WITH_true_THEN_sameObjectWithTrueAuthReturned() throws Exception {
    // arrange
    String expectedPrincipal = UUID.randomUUID().toString();

    SIPX509AuthenticationToken subject = new SIPX509AuthenticationToken(expectedPrincipal, false);

    // act
    SIPX509AuthenticationToken result = subject.withAuthenticated(true);

    // assert
    assertCtorCorrectValues(expectedPrincipal, result);
    assertThat(result.isAuthenticated()).isTrue();
  }

  private void assertCtorCorrectValues(
      String expectedPrincipal, SIPX509AuthenticationToken subject) {
    assertThat(subject.getAuthorities()).isEmpty();
    assertThat(subject.getCredentials()).isNull();
    assertThat(subject.getDetails()).isNull();
    assertThat(subject.getName()).isEqualTo(expectedPrincipal);
    assertThat(subject.getPrincipal()).isEqualTo(expectedPrincipal);
  }
}
