package one.x1f.sip.foundation.testkit.workflow;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.testkit.TestRunnerContext;
import one.x1f.sip.foundation.testkit.exception.NoRouteInvokerException;
import one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.ResultProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TestRunnerTest {

  private TestRunner testRunner;
  @Mock private ResultProcessor resultProcessor;

  @BeforeEach
  void setup() {
    testRunner = new TestRunner(resultProcessor, new TestRunnerContext());
  }

  @Test
  void When_runBuildTest_With_SuccessfulVerification_Then_Success() {
    // arrange
    TestCase testCase = mock(TestCase.class);
      when(testCase.getExecutionId()).thenReturn(UUID.randomUUID());
    when(testCase.getTestExecutionStatus())
        .thenReturn(new TestExecutionStatus("test").setSuccessfulExecution(true));

    // act + assert
    assertThat(testRunner.run(testCase)).isTrue();
    verify(testCase).run();
  }

  @Test
  void When_runBuildTest_Without_SuccessfulVerification_Then_Fail() {
    // arrange
    TestCase testCase = mock(TestCase.class);
    when(testCase.getTestExecutionStatus())
        .thenReturn(new TestExecutionStatus("test").setSuccessfulExecution(false));
      when(testCase.getExecutionId()).thenReturn(UUID.randomUUID());

    // act + assert
    assertThat(testRunner.run(testCase)).isFalse();
    verify(testCase).run();
  }

  @Test
  void When_runBuildTest_With_WorkflowException_Then_Fail() {
    // arrange
    TestCase testCase = mock(TestCase.class);
      when(testCase.getExecutionId()).thenReturn(UUID.randomUUID());
    TestExecutionStatus testExecutionStatus = new TestExecutionStatus("test");
    testExecutionStatus.setWorkflowException(new NoRouteInvokerException("routeId"));
    when(testCase.getTestExecutionStatus()).thenReturn(testExecutionStatus);

    // act + assert
    assertThat(testRunner.run(testCase)).isFalse();
    verify(testCase, times(0)).run();
  }

  @Test
  void When_runBuildTest_With_Exception_Then_Fail() {
    // arrange
    TestCase testCase = mock(TestCase.class, CALLS_REAL_METHODS);
      when(testCase.getExecutionId()).thenReturn(UUID.randomUUID());
    TestExecutionStatus testExecutionStatus = new TestExecutionStatus("test");
    testCase.setTestExecutionStatus(testExecutionStatus);
    doThrow(new RuntimeException()).when(testCase).run();
    doNothing().when(testCase).clearMocks();

    // act + assert
    assertThat(testRunner.run(testCase)).isFalse();
    verify(testCase).run();
    verify(testCase).clearMocks();
    assertThat(testCase.getTestExecutionStatus().getWorkflowException()).isPresent();
  }
}
