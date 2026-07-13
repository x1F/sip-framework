package one.x1f.sip.foundation.testkit.workflow;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.TestRunnerContext;
import one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.ResultProcessor;
import org.springframework.stereotype.Component;

/** Main class for running tests. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestRunner {
    public static final String TEST_EXECUTION_ID = "_SipTestExecutionId";
  private final ResultProcessor resultProcessor;
  private final TestRunnerContext testRunnerContext;

  /**
   * Run a single build test case.
   *
   * @param testCase {@link TestCase}
   */
  public boolean run(TestCase testCase) {
    TestExecutionStatus testExecutionStatus = executeTest(testCase);
    resultProcessor.process(testExecutionStatus);
    testRunnerContext.remove(testCase.getExecutionId());
    return testExecutionStatus.isSuccessfulExecution();
  }

  public TestExecutionStatus executeTest(TestCase testCase) {
    Optional<Exception> exception = testCase.getTestExecutionStatus().getWorkflowException();
    try {
      if (exception.isEmpty()) {
        testCase.run();
      } else {
        handleTestException(testCase, exception.get());
      }
    } catch (Exception e) {
      handleTestException(testCase, e);
    } finally {
      testCase.clearMocks();
    }
    return testCase.getTestExecutionStatus();
  }

  private void handleTestException(TestCase testCase, Exception e) {
    log.error("sip.testkit.workflow.testrunerror_{}_{}", testCase.getTestName(), e.getMessage());
    testCase.reportExecutionException(e);
  }
}
