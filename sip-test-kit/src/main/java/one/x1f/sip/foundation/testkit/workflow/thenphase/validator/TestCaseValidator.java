package one.x1f.sip.foundation.testkit.workflow.thenphase.validator;

import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;

/** Parent class for test result validators */
public interface TestCaseValidator {

  /**
   * Validates configured conditions based on data collected during test execution, like adapter
   * response or requests received by endpoint mocks.
   *
   * @param testExecutionStatus - provides test execution data and stores validation results
   */
  void validate(TestExecutionStatus testExecutionStatus);

  /**
   * Is a validator applicable for a test case based on parameters
   *
   * @return true if the validator is applicable, false otherwise and as default. Default value
   *     forces new implementation to think about usage of the validator.
   */
  default boolean isApplicable() {
    return false;
  }
}
