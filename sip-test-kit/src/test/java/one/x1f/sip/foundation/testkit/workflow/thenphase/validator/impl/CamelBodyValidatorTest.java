package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class CamelBodyValidatorTest {
  private static final String NAME_JOHN_AGE_30_CAR_NULL_EXAMPLE =
      "{\"name\":\"John\", \"age\":30, \"car\":null}";
  private static final ValidationResult VALIDATION_RESULT_SUCCESSFUL =
      new ValidationResult(true, "Body validation successful");
  private static final ValidationResult VALIDATION_RESULT_UNSUCCESSFUL =
      new ValidationResult(false, "Body validation unsuccessful");

  private final CamelBodyValidator bodyValidatorSubject = new CamelBodyValidator();
  private Exchange actual;
  private Exchange expected;

  @BeforeEach
  public void setUp() {
    // reset mocks
    actual = mock(Exchange.class, RETURNS_DEEP_STUBS);
    expected = mock(Exchange.class, RETURNS_DEEP_STUBS);
  }

  @Nested
  class ValidatorIsApplicable {
    @ParameterizedTest
    @ValueSource(strings = {EMPTY, "some value", "null"})
    void When_expectedBodyIsEmptyString_forAnyActualValue(String actualValue) {
      when(expected.getMessage().getBody()).thenReturn(EMPTY);
      lenient().when(actual.getMessage().getBody()).thenReturn(parseNull(actualValue));

      boolean isApplicable = bodyValidatorSubject.isApplicable(actual, expected);

      assertThat(isApplicable)
          .describedAs(
              "Body validator should be applicable if expected has empty string value, regardless of actual value")
          .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY, "some value", "null"})
    void When_expectedBodyHasValue_forAnyActualValue(String actualValue) {
      when(expected.getMessage().getBody()).thenReturn("any content");
      lenient().when(actual.getMessage().getBody()).thenReturn(parseNull(actualValue));

      boolean isApplicable = bodyValidatorSubject.isApplicable(actual, expected);

      assertThat(isApplicable)
          .describedAs(
              "Body validator should be applicable if expected has concrete value, regardless of actual value")
          .isTrue();
    }
  }

  @Nested
  class ValidatorIsNotApplicable {
    @ParameterizedTest
    @ValueSource(strings = {EMPTY, "some value", "null"})
    void When_expectedBodyIsNull_forAnyActualValue(String actualValue) {
      when(expected.getMessage().getBody()).thenReturn(null);
      lenient().when(actual.getMessage().getBody()).thenReturn(parseNull(actualValue));

      boolean isApplicable = bodyValidatorSubject.isApplicable(actual, expected);

      assertThat(isApplicable)
          .describedAs(
              "Body validator should not be applicable if expected value is null, regardless of actual value")
          .isFalse();
    }
  }

  @Nested
  class ValidationPasses {
    @Test
    void When_actualAndExpectedAreTheSame() {
      when(actual.getMessage().getBody()).thenReturn("some content");
      when(expected.getMessage().getBody()).thenReturn("some content");

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertEquals(VALIDATION_RESULT_SUCCESSFUL, validationResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", ""})
    void When_expectedIsEmptyAndActualIsNullOrEmpty(String actualValue) {
      when(actual.getMessage().getBody()).thenReturn(parseNull(actualValue));
      when(expected.getMessage().getBody()).thenReturn(EMPTY);

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_SUCCESSFUL);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"namespaceRelabeled.xml", "fieldsReordered.xml", "attributes_reordered.xml"})
    void When_twoXMLisComparedWithSameContentInDifferentFormats(String fileName)
        throws IOException {
      String xmlExample = readFile("test data/xml/original.xml");
      when(actual.getMessage().getBody()).thenReturn(xmlExample);

      String xmlExampleWithChangedNamespacePrefixes = readFile("test data/xml/" + fileName);
      when(expected.getMessage().getBody()).thenReturn(xmlExampleWithChangedNamespacePrefixes);

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_SUCCESSFUL);
    }

    @Test
    void When_twoXMLStringsDifferInWhitespacesBetweenTags() throws IOException {
      String xmlExample = readFile("test data/xml/original.xml");
      when(actual.getMessage().getBody()).thenReturn(xmlExample);
      when(expected.getMessage().getBody()).thenReturn(xmlExample.replace("<h:td>", " <h:td> "));

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_SUCCESSFUL);
    }

    @Test
    void When_twoJsonStringsDifferInWhitespacesBetweenQuotes() {
      String jsonExample = NAME_JOHN_AGE_30_CAR_NULL_EXAMPLE;
      when(actual.getMessage().getBody()).thenReturn(jsonExample);
      when(expected.getMessage().getBody()).thenReturn(jsonExample.replace(",", "  ,  "));
      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_SUCCESSFUL);
    }

    @Test
    void When_twoJsonStringsDifferInFieldsOrder() {
      String jsonExampleReordered = "{\"car\":null,\"age\":30, \"name\":\"John\"}";
      when(actual.getMessage().getBody()).thenReturn(NAME_JOHN_AGE_30_CAR_NULL_EXAMPLE);
      when(expected.getMessage().getBody()).thenReturn(jsonExampleReordered);
      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_SUCCESSFUL);
    }
  }

  @Nested
  class ValidationFails {
    @Test
    void When_actualAndExpectedAreDifferentStrings() {
      when(actual.getMessage().getBody()).thenReturn("some content");
      when(expected.getMessage().getBody()).thenReturn("some other content");

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_UNSUCCESSFUL);
    }

    @Test
    void When_expectedHasValueAndActualIsNull() {
      when(actual.getMessage().getBody()).thenReturn(null);
      when(expected.getMessage().getBody()).thenReturn("test");

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult).isEqualTo(VALIDATION_RESULT_UNSUCCESSFUL);
    }

    @Test
    void When_ActualAndExpectedAreDifferentXMLs_Then_describedDifferenceIsReturned()
        throws IOException {
      String xmlExample = readFile("test data/xml/original.xml");
      when(actual.getMessage().getBody()).thenReturn(xmlExample);
      when(expected.getMessage().getBody()).thenReturn(xmlExample.replace(":table>", ":mable>"));

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult.isSuccess()).isFalse();
      AssertionsForClassTypes.assertThat(validationResult.getMessage())
          .isNotBlank()
          .contains("mable");
    }

    @Test
    void
        When_ActualAndExpectedAreDifferentJSONsThatMismatchesInLetterCase_Then_describedDifferenceIsReturned() {
      String jsonExample = NAME_JOHN_AGE_30_CAR_NULL_EXAMPLE;
      when(actual.getMessage().getBody()).thenReturn(jsonExample);
      when(expected.getMessage().getBody()).thenReturn(jsonExample.replace("car", "CAR"));

      ValidationResult validationResult = bodyValidatorSubject.execute(actual, expected);

      assertThat(validationResult.isSuccess()).isFalse();
      assertThat(validationResult.getMessage())
          .contains("not equal: only on left={CAR=null}: only on right={car=null}");
    }
  }

  private static String parseNull(String actualValue) {
    return "null".equals(actualValue) ? null : actualValue;
  }

  private String readFile(String path) throws IOException {
    return FileUtils.readFileToString(
        new ClassPathResource(path).getFile(), StandardCharsets.UTF_8);
  }
}
