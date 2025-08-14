package one.x1f.sip.foundation.testkit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.testkit.util.SIPBatchTestArgumentSource;
import one.x1f.sip.foundation.testkit.workflow.TestCase;
import one.x1f.sip.foundation.testkit.workflow.TestCaseCollector;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.TestRunner;
import one.x1f.sip.foundation.testkit.workflow.givenphase.ReportActivityProxyExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.boot.test.context.SpringBootTest;

/** SIP batch test run */
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = "sip.testkit.batch-test=true",
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class SIPBatchTest {

  @Getter private final TestCaseCollector testCaseCollector;
  private final TestRunner testRunner;
  private final ReportActivityProxyExtension reportActivityProxyExtension;

  @BeforeAll
  void setup() {
    reportActivityProxyExtension.setTestCases(testCaseCollector.getTestCases());
  }

  @ArgumentsSource(SIPBatchTestArgumentSource.class)
  @DisplayName("Batch Tests")
  @ParameterizedTest(name = "{index}: {0}")
  void testCaseArguments(TestCase testCase) {
    assumeThat(isValid(testCase)).withFailMessage("No validation defined in then-expect").isTrue();
    assertThat(testRunner.run(testCase)).isTrue();
  }

  private boolean isValid(TestCase testCase) {
    TestExecutionStatus testExecutionStatus = testCase.getTestExecutionStatus();
    return !testExecutionStatus.getMockReports().isEmpty()
        || testExecutionStatus.getAdapterReport().getExpectedResponse() != null;
  }
}
