package one.x1f.sip.foundation.testkit.workflow;

import java.util.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.MockReport;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.SIPAdapterExecutionReport;
import org.apache.camel.Exchange;

/** Report for a test case */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TestExecutionStatus {
  private String testName;
  private boolean successfulExecution;
  private SIPAdapterExecutionReport adapterReport = new SIPAdapterExecutionReport();

  private Optional<Exception> workflowException = Optional.empty();
  private String workflowExceptionMessage;
  private Map<String, List<MockReport>> mockReports = new HashMap<>();

  private Map<String, Integer> iterations = new HashMap<>();
  private final Map<String, Integer> indexes = new HashMap<>();
  private UUID executionId;

  /**
   * Creates a new instance of TestReport
   *
   * @param testName Unique name of a test
   */
  public TestExecutionStatus(String testName) {
    this.testName = testName;
  }

  /**
   * Sets response which is expected to be returned by adapter during test. Even though it belongs
   * to enclosing {@link SIPAdapterExecutionReport} class, setter is also provided here to support
   * chain pattern.
   *
   * @param expectedAdapterResponse {@link Exchange} that is the result of a test run
   * @return Updated test execution status
   */
  public TestExecutionStatus setExpectedAdapterResponse(Exchange expectedAdapterResponse) {
    this.getAdapterReport().setExpectedResponse(expectedAdapterResponse);
    return this;
  }

  /**
   * Sets exception that was thrown by sip test kit, probably during test setup or validation phase.
   * Implicitly it will set workflowExceptionMessage
   *
   * @param workflowException exception thrown by SIP test kit
   */
  public void setWorkflowException(Exception workflowException) {
    this.workflowException = Optional.of(workflowException);
    this.workflowExceptionMessage = errorMessage(workflowException);
  }

  private String errorMessage(Exception e) {
    return "Error occurred during workflow of the test: "
        + e.getLocalizedMessage()
        + "\n location: "
        + Arrays.stream(e.getStackTrace()).findFirst();
  }

  public MockReport getMockReport(String mockId) {
    List<MockReport> reports = mockReports.computeIfAbsent(mockId, k -> new ArrayList<>());
    int index = indexes.getOrDefault(mockId, 0);

    if (index >= reports.size()) {
      MockReport newReport = new MockReport();
      reports.add(newReport);
    }

    MockReport report = reports.get(index);
    indexes.put(mockId, index + 1);
    return report;
  }
}
