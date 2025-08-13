package one.x1f.sip.foundation.security.authentication.x509;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class SIPX509TokenExtractorTest {

  public static final String EXPECTED_DN =
      "CN=Full Name, EMAILADDRESS=name@domain.de, O=IKOR Gmbh, C=DE";
  public static final String X_509_CERTIFICATE = "javax.servlet.request.X509Certificate";

  private final SIPX509TokenExtractor subject = new SIPX509TokenExtractor();

  @Test
  void WHEN_extract_WITH_validCert_THEN_certExtracted() {
    // arrange
    ((Logger) LoggerFactory.getLogger(SIPX509TokenExtractor.class)).setLevel(Level.INFO);
    X509Certificate certMock = mock(X509Certificate.class);
    X500Principal principal = new X500Principal(EXPECTED_DN);
    when(certMock.getSubjectX500Principal()).thenReturn(principal);

    X509Certificate[] certs = {certMock};

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(X_509_CERTIFICATE, certs);

    // act
    SIPX509AuthenticationToken result = subject.extract(request);

    // assert
    assertThat(result.getPrincipal()).asString().isEqualTo(EXPECTED_DN);
  }

  @Test
  void WHEN_extract_WITH_validCertTraceLogging_THEN_certExtracted() {
    // arrange
    Logger logger = (Logger) LoggerFactory.getLogger(SIPX509TokenExtractor.class);
    logger.setLevel(Level.TRACE);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    List<ILoggingEvent> logsList = listAppender.list;
    X509Certificate certMock = mock(X509Certificate.class);
    X500Principal principal = new X500Principal(EXPECTED_DN);
    when(certMock.getSubjectX500Principal()).thenReturn(principal);

    X509Certificate[] certs = {certMock};

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(X_509_CERTIFICATE, certs);

    // act
    SIPX509AuthenticationToken result = subject.extract(request);

    // assert
    assertThat(result.getPrincipal()).asString().isEqualTo(EXPECTED_DN);
    ILoggingEvent message = logsList.get(0);
    assertThat(message.getLevel()).isEqualTo(Level.TRACE);
    assertThat(message.getMessage()).isEqualTo("sip.security.x509.clientcertificate_{}");
  }

  @Test
  void WHEN_extract_WITH_nullCertificate_THEN_badCredentialsException() {
    // arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(X_509_CERTIFICATE, null);

    // act + assert
    assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> subject.extract(request));
  }

  @Test
  void WHEN_extract_WITH_emptyCertificates_THEN_badCredentialsException() {
    // arrange
    X509Certificate[] certs = {};

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(X_509_CERTIFICATE, certs);

    // act + assert
    assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> subject.extract(request));
  }

  @Test
  void WHEN_getTokenType_THEN_returnX509Type() {
    assertThat(subject.getTokenType()).isEqualTo(SIPX509AuthenticationToken.class);
  }
}
