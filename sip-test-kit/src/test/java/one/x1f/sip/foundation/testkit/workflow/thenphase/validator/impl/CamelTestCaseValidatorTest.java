// package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
// import one.x1f.sip.foundation.testkit.workflow.reporting.model.EndpointValidationOutcome;
// import one.x1f.sip.foundation.testkit.workflow.reporting.model.MockReport;
// import one.x1f.sip.foundation.testkit.workflow.reporting.model.SIPAdapterExecutionReport;
// import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
// import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
// import org.apache.camel.Exchange;
// import org.apache.camel.Message;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// class CamelTestCaseValidatorTest {
//
//  private static final String MOCK_REPORT_KEY = "key";
//  private static final String BODY = "body";
//  private static final String SUCCESS_MESSAGE = "success";
//  private static final String FAIL_MESSAGE = "unsuccessful";
//  private static final String HEADER_KEY = "headerkey";
//  private static final String HEADER_VALUE = "headervalue";
//  private CamelTestCaseValidator subject;
//  private ExchangeValidator exchangeValidator;
//  private Exchange actual;
//  private Exchange expected;
//  private Message actualMessage;
//  private Message expectedMessage;
//
//  @BeforeEach
//  void setUp() {
//    List<ExchangeValidator> validators = new ArrayList<>();
//    exchangeValidator = mock(ExchangeValidator.class);
//    validators.add(exchangeValidator);
//    subject = new CamelTestCaseValidator(validators);
//
//    actual = mock(Exchange.class);
//    expected = mock(Exchange.class);
//    actualMessage = mock(Message.class);
//    expectedMessage = mock(Message.class);
//    when(actual.getMessage()).thenReturn(actualMessage);
//    when(expected.getMessage()).thenReturn(expectedMessage);
//    when(actualMessage.getBody()).thenReturn(BODY);
//    when(actualMessage.getHeaders()).thenReturn(new HashMap<>());
//    when(expectedMessage.getHeaders()).thenReturn(new HashMap<>());
//    when(exchangeValidator.isApplicable(actual, expected)).thenReturn(true);
//  }
//
//  @Test
//  void When_validate_With_SuccessfulValidators_Expect_ValidationResultSuccessful() {
//    // arrange
//    TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
//    SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();
//    Map<String, MockReport> mockReports = new HashMap<>();
//    adapterReport.setActualResponse(actual);
//    adapterReport.setExpectedResponse(expected);
//    testExecutionStatus.setAdapterReport(adapterReport);
//    testExecutionStatus.setMockReports(mockReports);
//
//    when(exchangeValidator.execute(actual, expected))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//    Map<String, Object> expectedHeaders = new HashMap<>();
//    expectedHeaders.put(HEADER_KEY, HEADER_VALUE);
//    when(actualMessage.getHeader(HEADER_KEY)).thenReturn(HEADER_VALUE);
//    when(actualMessage.getHeader(HEADER_KEY, String.class)).thenReturn(HEADER_VALUE);
//    when(expectedMessage.getHeaders()).thenReturn(expectedHeaders);
//
//    // act
//    subject.validate(testExecutionStatus);
//
//    // assert
//    assertThat(testExecutionStatus.isSuccessfulExecution()).isTrue();
//    assertThat(testExecutionStatus.getAdapterReport().getValidatedHeaders())
//        .containsEntry(HEADER_KEY, HEADER_VALUE);
//  }
//
//  @Test
//  void When_validate_With_UnsuccessfulValidators_Expect_ValidationResultUnsuccessful() {
//    // arrange
//    TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
//    SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();
//    Map<String, MockReport> mockReports = new HashMap<>();
//    adapterReport.setActualResponse(actual);
//    adapterReport.setExpectedResponse(expected);
//    testExecutionStatus.setAdapterReport(adapterReport);
//    testExecutionStatus.setMockReports(mockReports);
//
//    when(exchangeValidator.execute(actual, expected))
//        .thenReturn(new ValidationResult(false, FAIL_MESSAGE));
//
//    // act
//    subject.validate(testExecutionStatus);
//
//    // assert
//    assertThat(testExecutionStatus.isSuccessfulExecution()).isFalse();
//  }
//
//  @Test
//  void When_validate_With_SuccessfulValidatorsAndMocks_Expect_EndpointValidationSuccessful() {
//    // arrange
//    TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
//    SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();
//
//    Map<String, MockReport> mockReports = new HashMap<>();
//    MockReport mockReport = new MockReport();
//    Exchange endpointActual = mock(Exchange.class);
//    Exchange endpointExpected = mock(Exchange.class);
//    Message endpointExpectedMessage = mock(Message.class);
//    when(endpointExpected.getMessage()).thenReturn(endpointExpectedMessage);
//    when(endpointExpectedMessage.getBody()).thenReturn(BODY);
//    mockReport.setActual(endpointActual);
//    mockReport.setExpected(endpointExpected);
//    mockReports.put(MOCK_REPORT_KEY, mockReport);
//    adapterReport.setActualResponse(actual);
//    adapterReport.setExpectedResponse(expected);
//    testExecutionStatus.setAdapterReport(adapterReport);
//    testExecutionStatus.setMockReports(mockReports);
//
//    when(exchangeValidator.execute(actual, expected))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//
//    when(exchangeValidator.isApplicable(endpointActual, endpointExpected)).thenReturn(true);
//    when(exchangeValidator.execute(endpointActual, endpointExpected))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//
//    assertThat(testExecutionStatus.getMockReports().get(MOCK_REPORT_KEY).getValidated())
//        .isEqualTo(EndpointValidationOutcome.SKIPPED);
//
//    // act
//    subject.validate(testExecutionStatus);
//
//    // assert
//    assertThat(testExecutionStatus.isSuccessfulExecution()).isTrue();
//    assertThat(testExecutionStatus.getMockReports().get(MOCK_REPORT_KEY).getValidated())
//        .isEqualTo(EndpointValidationOutcome.SUCCESSFUL);
//  }
//
//  @Test
//  void When_validate_With_UnsuccessfulValidatorsAndMocks_Expect_EndpointValidationUnsuccessful() {
//    // arrange
//    TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
//    SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();
//
//    Map<String, MockReport> mockReports = new HashMap<>();
//    MockReport mockReport = new MockReport();
//    Exchange endpointActual = mock(Exchange.class);
//    Exchange endpointExpected = mock(Exchange.class);
//    Message endpointExpectedMessage = mock(Message.class);
//    when(endpointExpected.getMessage()).thenReturn(endpointExpectedMessage);
//    when(endpointExpectedMessage.getBody()).thenReturn(BODY);
//    mockReport.setActual(endpointActual);
//    mockReport.setExpected(endpointExpected);
//    mockReports.put(MOCK_REPORT_KEY, mockReport);
//    adapterReport.setActualResponse(actual);
//    adapterReport.setExpectedResponse(expected);
//    testExecutionStatus.setAdapterReport(adapterReport);
//    testExecutionStatus.setMockReports(mockReports);
//
//    when(exchangeValidator.execute(actual, expected))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//
//    when(exchangeValidator.isApplicable(endpointActual, endpointExpected)).thenReturn(true);
//    when(exchangeValidator.execute(endpointActual, endpointExpected))
//        .thenReturn(new ValidationResult(false, FAIL_MESSAGE));
//
//    assertThat(testExecutionStatus.getMockReports().get(MOCK_REPORT_KEY).getValidated())
//        .isEqualTo(EndpointValidationOutcome.SKIPPED);
//
//    // act
//    subject.validate(testExecutionStatus);
//
//    // assert
//    assertThat(testExecutionStatus.isSuccessfulExecution()).isFalse();
//    assertThat(testExecutionStatus.getMockReports().get(MOCK_REPORT_KEY).getValidated())
//        .isEqualTo(EndpointValidationOutcome.UNSUCCESSFUL);
//  }
//
//  @Test
//  void When_validate_With_NullExpected_Expect_SkippedEndpointValidation() {
//    // arrange
//    TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
//    SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();
//
//    Map<String, MockReport> mockReports = new HashMap<>();
//    MockReport mockReport = new MockReport();
//    Exchange endpointActual = mock(Exchange.class);
//    Message endpointExpectedMessage = mock(Message.class);
//    when(endpointExpectedMessage.getBody()).thenReturn(BODY);
//    mockReport.setActual(endpointActual);
//    mockReports.put(MOCK_REPORT_KEY, mockReport);
//    adapterReport.setActualResponse(actual);
//    adapterReport.setExpectedResponse(null);
//    testExecutionStatus.setAdapterReport(adapterReport);
//    testExecutionStatus.setMockReports(mockReports);
//
//    when(exchangeValidator.execute(actual, null))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//
//    when(exchangeValidator.isApplicable(endpointActual, null)).thenReturn(true);
//    when(exchangeValidator.execute(endpointActual, null))
//        .thenReturn(new ValidationResult(true, SUCCESS_MESSAGE));
//
//    // act
//    subject.validate(testExecutionStatus);
//
//    // assert
//    assertThat(testExecutionStatus.isSuccessfulExecution()).isTrue();
//    assertThat(testExecutionStatus.getMockReports().get(MOCK_REPORT_KEY).getValidated())
//        .isEqualTo(EndpointValidationOutcome.SKIPPED);
//  }
// }
