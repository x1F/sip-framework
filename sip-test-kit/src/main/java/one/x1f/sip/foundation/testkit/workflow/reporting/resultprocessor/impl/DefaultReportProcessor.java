package one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.impl;

import freemarker.template.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.ResultProcessor;
import org.apache.commons.exec.LogOutputStream;
import org.springframework.stereotype.Component;

/** Default result processor used for logging test execution results. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultReportProcessor implements ResultProcessor {

  private static final String TEMPLATE_NAME = "report-template.ftl";
  private final Configuration templateConfiguration;

  /**
   * Processes and logs executed test
   *
   * @param testExecutionStatus {@link TestExecutionStatus}
   */
  @Override
  public void process(TestExecutionStatus testExecutionStatus) {
    Map<String, Object> templateValues = new HashMap<>();
    templateValues.put("report", testExecutionStatus);

    logReports(templateValues);
  }

  private void logReports(Map<String, Object> reports) {
    try {
      displayReportsViaTemplate(reports);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
    }
  }

  private void displayReportsViaTemplate(Map<String, Object> templateValues)
      throws IOException, TemplateException {
    LogOutputStream outputStream = new LogbackOutputStream();
    try (Writer consoleWriter = new OutputStreamWriter(outputStream)) {
      Template template = templateConfiguration.getTemplate(TEMPLATE_NAME);
      template.process(templateValues, consoleWriter);
    }
  }
}
