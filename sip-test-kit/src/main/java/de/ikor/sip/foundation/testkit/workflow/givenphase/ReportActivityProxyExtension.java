package de.ikor.sip.foundation.testkit.workflow.givenphase;

import static de.ikor.sip.foundation.testkit.configurationproperties.models.MessageProperties.mapToMessageProperties;

import de.ikor.sip.foundation.core.proxies.ProcessorProxy;
import de.ikor.sip.foundation.core.proxies.extension.ProxyExtension;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import de.ikor.sip.foundation.testkit.workflow.TestCase;
import de.ikor.sip.foundation.testkit.workflow.TestExecutionStatus;
import de.ikor.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import java.util.List;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

/** Proxy extensions for tracking activity of mocked ProcessorProxy */
@Slf4j
@Configuration
public class ReportActivityProxyExtension implements ProxyExtension {
  @Setter private List<TestCase> testCases;

  @Override
  public void run(ProcessorProxy proxy, Exchange original, Exchange current) {
    findTestReport(original)
        .getMockReport(proxy.getId())
        .setActual(original)
        .setActualMessage(mapToMessageProperties(original));
  }

  private String getTestName(Exchange original) {
    return StringUtils.firstNonBlank(
        original.getProperty(RouteInvoker.TEST_NAME_HEADER, String.class),
        original.getMessage().getHeader(RouteInvoker.TEST_NAME_HEADER, String.class));
  }

  @Override
  public boolean isApplicable(ProcessorProxy proxy, Exchange original, Exchange current) {
    return isTest(original) && proxy.isEndpointProcessor() && hasTestCase(original);
  }

  private boolean isTest(Exchange exchange) {
    return "true".equals(exchange.getProperty(ProcessorProxy.TEST_MODE_HEADER, String.class))
        || "true"
            .equals(exchange.getMessage().getHeader(ProcessorProxy.TEST_MODE_HEADER, String.class));
  }

  private TestExecutionStatus findTestReport(Exchange exchange) {
    String testName = getTestName(exchange);
    Optional<TestCase> tc =
        testCases.stream().filter(testCase -> testCase.getTestName().equals(testName)).findFirst();
    if (tc.isEmpty()) {
      throw SIPFrameworkException.init("Test case with name %s could not be found!", testName);
    }
    return tc.get().getTestExecutionStatus();
  }

  private boolean hasTestCase(Exchange exchange) {
    return testCases != null
        && testCases.stream()
            .anyMatch(testCase -> testCase.getTestName().equals(getTestName(exchange)));
  }
}
