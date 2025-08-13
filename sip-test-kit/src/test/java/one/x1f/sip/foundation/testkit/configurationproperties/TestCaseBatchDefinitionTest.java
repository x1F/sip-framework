package one.x1f.sip.foundation.testkit.configurationproperties;

import static one.x1f.sip.foundation.testkit.config.TestCasesConfig.*;
import static one.x1f.sip.foundation.testkit.configurationproperties.TestCaseBatchDefinition.DUPLICATE_TEST_TITLE_MESSAGE;
import static one.x1f.sip.foundation.testkit.util.TestCaseDefinitionValidator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

class TestCaseBatchDefinitionTest {

  private static final String TEST_NAME = "Test name";
  private static final String ENDPOINT_ID = "endpointId";
  private static final String CONNECTOR_ID = "connectorId";

  private TestCaseBatchDefinition subject;

  private TestCaseDefinition testCaseDefinition;

  @BeforeEach
  void setup() {
    subject = new TestCaseBatchDefinition();
    testCaseDefinition = createValidTestCaseDefinition();
    subject.setTestCaseDefinitions(List.of(testCaseDefinition));
  }

  @Test
  void WHEN_classTypeIsTestCaseBatchDefinition_THEN_expectTrue() {
    // arrange, act & assert
    assertThat(subject.supports(TestCaseBatchDefinition.class)).isTrue();
  }

  @Test
  void WHEN_classTypeIsNotTestCaseBatchDefinition_THEN_expectFalse() {
    // arrange, act & assert
    assertThat(subject.supports(Object.class)).isFalse();
  }

  @Test
  void WHEN_validTestCaseDefinition_THEN_noException() {
    // act
    subject.validate(subject, mock(Errors.class));

    // assert
    assertThat(subject.getTestCaseDefinitions()).hasSize(1);
  }

  @Test
  void WHEN_noTitle_THEN_SIPFrameworkException() {
    // arrange
    testCaseDefinition.setTitle(null);

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(MISSING_TITLE_EXCEPTION_MSG);
  }

  @Test
  void WHEN_noWhenExecuteSection_THEN_SIPFrameworkException() {
    // arrange
    testCaseDefinition.setWhenExecute(null);

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(REQUIRED_WHEN_EXECUTE_EXCEPTION_MSG, TEST_NAME));
  }

  @Test
  void WHEN_noEndpointIdAndNoConnectorIdForWhenExecuteSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties(null, "", null);
    testCaseDefinition.setWhenExecute(endpointProperties);

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(MISSING_PARAMETERS_EXCEPTION_MSG, WHEN_EXECUTE, TEST_NAME));
  }

  @Test
  void WHEN_givenBothEndpointIdAndConnectorIdForWhenExecuteSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties(ENDPOINT_ID, CONNECTOR_ID, null);
    testCaseDefinition.setWhenExecute(endpointProperties);

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(BOTH_PARAMETERS_PROVIDED_EXCEPTION_MSG, WHEN_EXECUTE, TEST_NAME));
  }

  @Test
  void WHEN_noEndpointIdAndNoConnectorIdForWithMocksSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties("", null, null);
    testCaseDefinition.setWithMocks(List.of(endpointProperties));

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(MISSING_PARAMETERS_EXCEPTION_MSG, WITH_MOCKS, TEST_NAME));
  }

  @Test
  void WHEN_givenBothEndpointIdAndConnectorIdForWithMocksSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties(ENDPOINT_ID, CONNECTOR_ID, null);
    testCaseDefinition.setWithMocks(List.of(endpointProperties));

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(BOTH_PARAMETERS_PROVIDED_EXCEPTION_MSG, WITH_MOCKS, TEST_NAME));
  }

  @Test
  void WHEN_noEndpointIdAndNoConnectorIdForThenExpectSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties(null, "", null);
    testCaseDefinition.setThenExpect(List.of(endpointProperties));

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(MISSING_PARAMETERS_EXCEPTION_MSG, THEN_EXPECT, TEST_NAME));
  }

  @Test
  void WHEN_givenBothEndpointIdAndConnectorIdForThenExpectSection_THEN_SIPFrameworkException() {
    // arrange
    EndpointProperties endpointProperties = new EndpointProperties(ENDPOINT_ID, CONNECTOR_ID, null);
    testCaseDefinition.setThenExpect(List.of(endpointProperties));

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(BOTH_PARAMETERS_PROVIDED_EXCEPTION_MSG, THEN_EXPECT, TEST_NAME));
  }

  @Test
  void WHEN_givenMultipleTestsWithSameName_THEN_SIPFrameworkException() {
    // arrange
    TestCaseDefinition duplicateTest = createValidTestCaseDefinition();
    subject.setTestCaseDefinitions(List.of(testCaseDefinition, duplicateTest));

    // act & assert
    assertThatThrownBy(() -> subject.validate(subject, mock(Errors.class)))
        .isInstanceOf(SIPFrameworkException.class)
        .hasMessage(String.format(DUPLICATE_TEST_TITLE_MESSAGE, TEST_NAME));
  }

  private TestCaseDefinition createValidTestCaseDefinition() {
    TestCaseDefinition definition = new TestCaseDefinition();
    definition.setTitle(TEST_NAME);
    EndpointProperties endpointProperties = new EndpointProperties(ENDPOINT_ID, null, null);
    definition.setWhenExecute(endpointProperties);
    definition.setWithMocks(List.of(endpointProperties));
    definition.setThenExpect(List.of(endpointProperties));
    return definition;
  }
}
