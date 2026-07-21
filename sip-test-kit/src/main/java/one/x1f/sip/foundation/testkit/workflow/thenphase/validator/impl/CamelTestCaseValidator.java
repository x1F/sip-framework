package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.EndpointValidationOutcome;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.MockReport;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.SIPAdapterExecutionReport;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.ExchangeValidator;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.TestCaseValidator;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/** Batch test validator for Camel */
@Slf4j
@Component
@AllArgsConstructor
public class CamelTestCaseValidator implements TestCaseValidator {
  private final List<ExchangeValidator> exchangeValidators;

  /** Validates actual of test execution and forwards it to report service */
  @Override
  public void validate(TestExecutionStatus testExecutionStatus) {
    SIPAdapterExecutionReport adapterReport = testExecutionStatus.getAdapterReport();
    Map<String, List<MockReport>> mockReports = testExecutionStatus.getMockReports();

    if (adapterReport.getActualResponse() != null) {
      this.validateAdapterResponse(adapterReport, testExecutionStatus.getExecutionId());
    }
    mockReports
        .values()
        .forEach(reports -> validateMockReports(reports, testExecutionStatus.getExecutionId()));

    boolean isAdapterResultExpected =
        evaluateValidationResults(adapterReport.getValidationResults());
    boolean areAllMocksExpected =
        mockReports.values().stream().flatMap(Collection::stream).noneMatch(this::isNotSuccess);

    testExecutionStatus.setSuccessfulExecution(isAdapterResultExpected && areAllMocksExpected);
  }

  private void validateAdapterResponse(
      SIPAdapterExecutionReport adapterExecutionReport, UUID executionId) {
    Exchange actual = adapterExecutionReport.getActualResponse();
    Exchange expected = adapterExecutionReport.getExpectedResponse();
    List<ValidationResult> adapterValidationResults = runValidators(actual, expected, executionId);
    adapterExecutionReport
        .setValidationResults(adapterValidationResults)
        .setValidatedHeaders(extractValidatedHeaders(actual, expected))
        .setAdapterExceptionMessage(actual.getException());
  }

  private void validateMockReports(List<MockReport> mockReportMap, UUID executionId) {
    mockReportMap.stream()
        .filter(mockReport -> mockReport.getExpected() != null)
        .forEach(mockReport -> fillMockReport(mockReport, executionId));
  }

  private void fillMockReport(MockReport mockReport, UUID executionId) {
    List<ValidationResult> endpointValidationResultList =
        runValidators(mockReport.getActual(), mockReport.getExpected(), executionId);
    mockReport.setValidated(
        evaluateValidationResults(endpointValidationResultList)
            ? EndpointValidationOutcome.SUCCESSFUL
            : EndpointValidationOutcome.UNSUCCESSFUL);
    mockReport.setValidatedHeaders(
        extractValidatedHeaders(mockReport.getActual(), mockReport.getExpected()));
    mockReport.setValidationResults(endpointValidationResultList);
  }

  private boolean isNotSuccess(MockReport mockReport) {
    return mockReport.getValidated().equals(EndpointValidationOutcome.UNSUCCESSFUL);
  }

  private HashMap<String, Object> extractValidatedHeaders(
      Exchange executionResult, Exchange expectedResponse) {
    HashMap<String, Object> validatedHeaders = new HashMap<>();
    if (executionResult == null || expectedResponse == null) {
      return validatedHeaders;
    }
    expectedResponse
        .getMessage()
        .getHeaders()
        .keySet()
        .forEach(
            key -> {
              if (executionResult.getMessage().getHeader(key) != null) {
                validatedHeaders.put(
                    key, executionResult.getMessage().getHeader(key, String.class));
              }
            });
    return validatedHeaders;
  }

  private boolean evaluateValidationResults(List<ValidationResult> validationResultList) {
    return validationResultList.stream().allMatch(ValidationResult::isSuccess);
  }

  private List<ValidationResult> runValidators(
      Exchange executionResult, Exchange expectedResponse, UUID executionId) {
    return this.exchangeValidators.stream()
        .filter(validator -> validator.isApplicable(executionResult, expectedResponse))
        .map(validator -> validator.execute(executionResult, expectedResponse, executionId))
        .toList();
  }

  @Override
  public boolean isApplicable() {
    return true;
  }
}
