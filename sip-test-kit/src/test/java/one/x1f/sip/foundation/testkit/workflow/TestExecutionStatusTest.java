package one.x1f.sip.foundation.testkit.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestExecutionStatusTest {

  private static final String EXCEPTION_MESSAGE = "exception message";
  private static final String MOCK_REPORT_KEY = "any";
  TestExecutionStatus subject;

  @BeforeEach
  void setUp() {
    subject = new TestExecutionStatus("test name");
  }

  @Test
  void GIVEN_exchange_WHEN_setExpectedAdapterResponse_THEN_adapterReportUpdated() {
    // arrange
    Exchange exchange = mock(Exchange.class);

    // act
    subject.setExpectedAdapterResponse(exchange);

    // assert
    assertThat(subject.getAdapterReport().getExpectedResponse()).isEqualTo(exchange);
  }

  @Test
  void GIVEN_runtimeException_WHEN_setWorkflowException_THEN_workflowExceptionAndMessageSet() {
    // arrange
    Exception ex = new RuntimeException(EXCEPTION_MESSAGE);

    // act
    subject.setWorkflowException(ex);

    // assert
    assertThat(subject.getWorkflowException()).isPresent();
    assertThat(subject.getWorkflowException()).contains(ex);
    assertThat(subject.getWorkflowExceptionMessage())
        .startsWith("Error occurred during workflow of the test: " + EXCEPTION_MESSAGE);
  }

  @Test
  void GIVEN_noMockReport_WHEN_getMockReport_THEN_addNewMockReport() {
    // act + assert
    assertThat(subject.getMockReports().get(MOCK_REPORT_KEY)).isNull();
    assertThat(subject.getMockReport(MOCK_REPORT_KEY)).isNotNull();
    assertThat(subject.getMockReports().get(MOCK_REPORT_KEY)).isNotNull();
  }
}
