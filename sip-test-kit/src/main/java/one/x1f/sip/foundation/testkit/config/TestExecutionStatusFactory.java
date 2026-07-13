package one.x1f.sip.foundation.testkit.config;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.parseExchangeProperties;

import java.util.*;

import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.testkit.configurationproperties.TestCaseDefinition;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.reporting.model.MockReport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestExecutionStatusFactory {
  private final CamelContext camelContext;

  TestExecutionStatus generateTestReport(TestCaseDefinition testCaseDefinition, UUID executionId) {
    String testName = testCaseDefinition.getTitle();
    return new TestExecutionStatus()
        .setTestName(testName)
        .setExecutionId(executionId)
        .setExpectedAdapterResponse(getExpectedAdapterResponse(testCaseDefinition))
        .setMockReports(getMockReports(testCaseDefinition));
  }

  private Map<String, List<MockReport>> getMockReports(TestCaseDefinition testCaseDefinition) {
    List<EndpointProperties> expectedEndpointResponses =
        expectedEndpointResponses(testCaseDefinition);
    Map<String, List<MockReport>> reportMap = new HashMap<>();
    expectedEndpointResponses.forEach(
        endpointProperty -> {
          var reports =
              reportMap.computeIfAbsent(endpointProperty.getEndpointId(), k -> new ArrayList<>());
          reports.add(
              new MockReport()
                  .setExpected(parseExchangeProperties(endpointProperty, camelContext)));
        });
    return reportMap;
  }

  private Exchange getExpectedAdapterResponse(TestCaseDefinition testCaseDefinition) {
    String startingEndpoint = testCaseDefinition.getWhenExecute().getEndpointId();
    String startingConnector = testCaseDefinition.getWhenExecute().getConnectorId();
    EndpointProperties endpointProperties =
        IterableUtils.find(
            testCaseDefinition.getThenExpect(),
            endpoint ->
                endpoint.getEndpointId().equals(startingEndpoint)
                    || endpoint.getConnectorId().equals(startingConnector));
    return parseExchangeProperties(endpointProperties, camelContext);
  }

  private List<EndpointProperties> expectedEndpointResponses(
      TestCaseDefinition testCaseDefinition) {
    String expectedAdapterResponseId = testCaseDefinition.getWhenExecute().getEndpointId();
    String expectedAdapterConnectorId = testCaseDefinition.getWhenExecute().getConnectorId();
    return testCaseDefinition.getThenExpect().isEmpty()
        ? new ArrayList<>()
        : testCaseDefinition.getThenExpect().stream()
            .filter(
                endpointProperties ->
                    !endpointProperties.getEndpointId().equals(expectedAdapterResponseId)
                        && !endpointProperties.getConnectorId().equals(expectedAdapterConnectorId))
            .toList();
  }
}
