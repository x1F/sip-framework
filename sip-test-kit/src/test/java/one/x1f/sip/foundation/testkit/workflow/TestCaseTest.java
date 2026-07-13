package one.x1f.sip.foundation.testkit.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.TestCaseValidator;
import one.x1f.sip.foundation.testkit.workflow.whenphase.ExecutionWrapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestCaseTest {

  private static final String TEST_NAME = "name";
  public static final String EXCEPTION_MESSAGE = "exception message";
  TestCase subject;
  List<Mock> mocks;
  ExecutionWrapper executionWrapper;
  TestCaseValidator validator;
  TestExecutionStatus testExecutionStatus;
  Mock mock;

  @BeforeEach
  void setUp() {
    mock = mock(Mock.class);
    mocks = new ArrayList<>();
    mocks.add(mock);
    executionWrapper = mock(ExecutionWrapper.class);
    validator = mock(TestCaseValidator.class);
    testExecutionStatus = new TestExecutionStatus(TEST_NAME);
    subject = new TestCase(TEST_NAME, mocks, validator, testExecutionStatus, UUID.randomUUID());
    subject.setExecutionWrapper(executionWrapper);
  }

  @Test
  void WHEN_run_THEN_allPhasesExecuted() {
    // arrange
    Exchange exchange = mock(Exchange.class);
    Message message = mock(Message.class);
    when(message.getBody()).thenReturn("message");
    when(exchange.getMessage()).thenReturn(message);
    when(executionWrapper.execute()).thenReturn(Optional.of(exchange));
    doNothing().when(validator).validate(testExecutionStatus);

    // act
    subject.run();

    // assert
    verify(mock, times(1)).setBehavior(testExecutionStatus);
    verify(validator, times(1)).validate(testExecutionStatus);
    assertThat(testExecutionStatus.getAdapterReport().getActualResponse()).isEqualTo(exchange);
  }

  @Test
  void WHEN_clearMocks_THEN_MockClearCalled() {
    // act
    subject.clearMocks();

    // assert
    verify(mock, times(1)).clear();
  }

  @Test
  void GIVEN_runtimeException_WHEN_reportExecutionException_THEN_validateFailedTestExecution() {
    // arrange
    RuntimeException ex = new RuntimeException(EXCEPTION_MESSAGE);

    // act
    subject.reportExecutionException(ex);

    // assert
    assertThat(testExecutionStatus.isSuccessfulExecution()).isFalse();
    assertThat(testExecutionStatus.getWorkflowException()).isPresent();
    assertThat(testExecutionStatus.getWorkflowException()).contains(ex);
    assertThat(testExecutionStatus.getWorkflowExceptionMessage()).contains(EXCEPTION_MESSAGE);
  }
}
