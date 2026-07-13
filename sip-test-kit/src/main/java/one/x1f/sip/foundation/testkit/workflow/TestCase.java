package one.x1f.sip.foundation.testkit.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.Data;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.TestCaseValidator;
import one.x1f.sip.foundation.testkit.workflow.whenphase.ExecutionWrapper;
import org.apache.camel.Exchange;

/** Class containing everything necessary for test execution. */
@Data
public class TestCase {
  private String testName;
  private List<Mock> mocks;
  private ExecutionWrapper executionWrapper;
  private TestCaseValidator validator;
  private TestExecutionStatus testExecutionStatus;
  private final UUID executionId;

  public TestCase(
      String testName,
      List<Mock> mocks,
      TestCaseValidator validator,
      TestExecutionStatus testExecutionStatus,
      UUID executionId) {
    this.testName = testName;
    this.mocks = mocks;
    this.validator = validator;
    this.testExecutionStatus = testExecutionStatus;
    this.executionId = executionId;
  }

  /** Start Test execution */
  public void run() {
    executeGivenPhase();
    Optional<Exchange> optionalExchange = executeWhenPhase();
    optionalExchange.ifPresent(
        value -> testExecutionStatus.getAdapterReport().setActualResponse(value));
    executeThenPhase(testExecutionStatus);
  }

  /** Clear mocks after execution */
  public void clearMocks() {
    mocks.forEach(Mock::clear);
  }

  /**
   * Signal this test case that the Exception happened so the TestReport can be updated
   *
   * @param exception that happened
   */
  public void reportExecutionException(Exception exception) {
    testExecutionStatus.setSuccessfulExecution(false).setWorkflowException(exception);
  }

  /** Prepare and active mocks for test */
  private void executeGivenPhase() {
    for (Mock mock : mocks) {
      mock.setBehavior(testExecutionStatus);
    }
  }

  private Optional<Exchange> executeWhenPhase() {
    return executionWrapper.execute();
  }

  private void executeThenPhase(TestExecutionStatus testExecutionStatus) {
    validator.validate(testExecutionStatus);
  }
}
