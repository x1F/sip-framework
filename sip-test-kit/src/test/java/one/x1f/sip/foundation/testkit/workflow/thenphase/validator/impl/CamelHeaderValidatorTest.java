package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CamelHeaderValidatorTest {

  private static final String HEADER_VALUE = "value";
  private static final String HEADER_KEY = "key";

  CamelHeaderValidator headerValidator;
  Exchange result;
  Exchange expected;
  Map<String, Object> expectedHeaders;
  Map<String, Object> actualHeaders;

  @BeforeEach
  void setUp() {
    expectedHeaders = new HashMap<>();
    actualHeaders = new HashMap<>();
    expectedHeaders.put(HEADER_KEY, HEADER_VALUE);
    actualHeaders.put(HEADER_KEY, HEADER_VALUE);
    headerValidator = new CamelHeaderValidator();
    result = mock(Exchange.class, RETURNS_DEEP_STUBS);
    expected = mock(Exchange.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void When_execute_With_Matching_Expect_Success() {
    Message resultMessage = mock(Message.class);
    Message expectedMessage = mock(Message.class);
    when(result.getMessage()).thenReturn(resultMessage);
    when(expected.getMessage()).thenReturn(expectedMessage);
    when(resultMessage.getHeader(HEADER_KEY)).thenReturn(HEADER_VALUE);
    when(resultMessage.getHeader(HEADER_KEY, String.class)).thenReturn(HEADER_VALUE);
    when(expectedMessage.getHeader(HEADER_KEY, String.class)).thenReturn(HEADER_VALUE);
    ValidationResult validationResultSuccessful =
        new ValidationResult(true, "Header validation successful");

    ValidationResult validationResultSubject = headerValidator.execute(result, expected);

    assertThat(validationResultSubject).isEqualTo(validationResultSuccessful);
  }

  @Test
  void When_execute_With_NotMatching_Expect_Fail() {
    Message resultMessage = mock(Message.class);
    Message expectedMessage = mock(Message.class);
    when(result.getMessage()).thenReturn(resultMessage);
    when(expected.getMessage()).thenReturn(expectedMessage);
    when(resultMessage.getHeader(HEADER_KEY, String.class)).thenReturn("none");
    when(expectedMessage.getHeaders()).thenReturn(expectedHeaders);
    ValidationResult validationResult =
        new ValidationResult(false, "Header validation unsuccessful");

    ValidationResult validationResultSubject = headerValidator.execute(result, expected);

    assertThat(validationResultSubject).isEqualTo(validationResult);
  }

  @Test
  void When_isApplicable_Expect_Success() {
    when(expected.getMessage().getHeaders()).thenReturn(expectedHeaders);

    assertThat(headerValidator.isApplicable(result, expected)).isTrue();
  }

  @Test
  void When_isNotApplicable_Expect_Fail() {
    when(expected.getMessage().getHeaders()).thenReturn(new HashMap<>());

    assertThat(headerValidator.isApplicable(result, expected)).isFalse();
  }
}
