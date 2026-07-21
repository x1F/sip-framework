package one.x1f.sip.foundation.testkit.config;

import static one.x1f.sip.foundation.core.declarative.RoutesRegistry.SIP_ENDPOINT_PROCESSOR_SUFFIX;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.parseAndEvaluateExchangeProperties;
import static one.x1f.sip.foundation.testkit.workflow.TestRunner.TEST_EXECUTION_ID;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.declarative.RouteRole;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorType;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.TestRunnerContext;
import one.x1f.sip.foundation.testkit.configurationproperties.TestCaseBatchDefinition;
import one.x1f.sip.foundation.testkit.configurationproperties.TestCaseDefinition;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import one.x1f.sip.foundation.testkit.exception.NoRouteInvokerException;
import one.x1f.sip.foundation.testkit.workflow.TestCase;
import one.x1f.sip.foundation.testkit.workflow.TestCaseCollector;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.givenphase.MockFactory;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.TestCaseValidator;
import one.x1f.sip.foundation.testkit.workflow.whenphase.ExecutionWrapper;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvokerFactory;
import org.apache.camel.CamelContext;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Context;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Configuration class used for creation of batch test cases based on test definitions. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestCasesConfig {

  public static final String WHEN_EXECUTE = "when-execute";
  public static final String WITH_MOCKS = "with-mocks";
  public static final String THEN_EXPECT = "then-expect";

  private final RouteInvokerFactory routeInvokerFactory;
  private final MockFactory mockFactory;
  private final CamelContext camelContext;
  private final TestCaseValidator testCaseValidator;
  private final TestExecutionStatusFactory executionStatusFactory;
  private final Optional<TestCaseBatchDefinition> testCaseBatchDefinition;
  private final TestCaseCollector testCaseCollector;
  private final Optional<RoutesRegistry> routesRegistry;
  private final TestRunnerContext testRunnerContext;

  private final Optional<DeclarationsRegistryApi> declarationsRegistry;

  /** Creates test cases based on batch test cases definition. */
  @EventListener(ApplicationReadyEvent.class)
  public void generateTestCases() {
    List<TestCase> testCases = new LinkedList<>();
    if (testCaseBatchDefinition.isPresent()) {
      for (TestCaseDefinition testCaseDefinition :
          testCaseBatchDefinition.get().getTestCaseDefinitions()) {
        testCases.add(generateTestCase(testCaseDefinition));
      }
    }
    testCaseCollector.setTestCases(testCases);
  }

  /**
   * Creation and initialisation of a single test case from a definition.
   *
   * @param testCaseDefinition Definition of a single test case {@link TestCaseDefinition}
   * @return a new test case {@link TestCase}
   */
  public TestCase generateTestCase(TestCaseDefinition testCaseDefinition) {
    String testName = testCaseDefinition.getTitle();
    List<String> realEndpoints = testCaseDefinition.getRealEndpoints();
    declarationsRegistry.ifPresent(registry -> validateConnectors(testCaseDefinition));
    if (routesRegistry.isPresent()) {
      initTestCaseDefinitionEndpoints(testCaseDefinition, routesRegistry.get());
      realEndpoints =
          testCaseDefinition.getRealEndpoints().stream()
              .map(
                  connectorId ->
                      routesRegistry
                          .get()
                          .getRouteIdByConnectorIdAndRole(connectorId, RouteRole.EXTERNAL_ENDPOINT))
              .toList();
    }

    var executionId = UUID.randomUUID();

    var context = testRunnerContext.getOrCreate(executionId);

    testCaseDefinition
        .getVariables()
        .forEach((key, value) -> context.getBindings("js").putMember(key, value));

    var mocks = getMocks(testName, testCaseDefinition, context);

    var testCase =
        new TestCase(
            testName,
            mocks,
            testCaseValidator,
            executionStatusFactory.generateTestReport(testCaseDefinition, executionId),
            executionId);

    var exchange =
        parseAndEvaluateExchangeProperties(
            testCaseDefinition.getWhenExecute(), camelContext, context);
    exchange.setProperty(TEST_EXECUTION_ID, testCase.getExecutionId());

    try {
      RouteInvoker invoker = routeInvokerFactory.getInstance(exchange);

      testCase.setExecutionWrapper(
          new ExecutionWrapper(testName, exchange, invoker, realEndpoints));
    } catch (NoRouteInvokerException e) {
      testCase.reportExecutionException(e);
    }
    return testCase;
  }

  private void initTestCaseDefinitionEndpoints(
      TestCaseDefinition definition, RoutesRegistry registry) {
    setEndpointBasedOnConnectorId(
        definition.getWhenExecute(), RouteRole.CONNECTOR_REQUEST_ORCHESTRATION, registry);
    definition
        .getWithMocks()
        .forEach(
            properties ->
                setEndpointBasedOnConnectorId(properties, RouteRole.EXTERNAL_ENDPOINT, registry));
    definition
        .getThenExpect()
        .forEach(
            properties ->
                setEndpointBasedOnConnectorId(properties, RouteRole.EXTERNAL_ENDPOINT, registry));
  }

  private void setEndpointBasedOnConnectorId(
      EndpointProperties properties, RouteRole role, RoutesRegistry registry) {
    if (StringUtils.isNotEmpty(properties.getConnectorId())) {
      String routeId = registry.getRouteIdByConnectorIdAndRole(properties.getConnectorId(), role);
      if (routeId == null) {
        throw SIPFrameworkException.init(
            "There is no connector with id %s", properties.getConnectorId());
      }
      properties.setEndpointId(routeId + SIP_ENDPOINT_PROCESSOR_SUFFIX);
    } else {
      String connectorId = registry.getConnectorIdByExternalEndpointId(properties.getEndpointId());
      if (connectorId == null) {
        throw SIPFrameworkException.init(
            "There is no connector for endpoint with id %s", properties.getEndpointId());
      }
      properties.setConnectorId(connectorId);
    }
  }

  private List<Mock> getMocks(
      String testName, TestCaseDefinition testCaseDefinition, Context context) {
    return testCaseDefinition.getWithMocks().stream()
        .map(
            connectionProperties ->
                mockFactory.newMockInstance(
                    testName,
                    parseAndEvaluateExchangeProperties(
                        connectionProperties, camelContext, context)))
        .toList();
  }

  private void validateConnectors(TestCaseDefinition testCaseDefinition) {
    validateConnectorType(testCaseDefinition.getWhenExecute(), ConnectorType.IN, WHEN_EXECUTE);
    testCaseDefinition
        .getWithMocks()
        .forEach(properties -> validateConnectorType(properties, ConnectorType.OUT, WITH_MOCKS));
  }

  private void validateConnectorType(
      EndpointProperties properties, ConnectorType type, String definitionPart) {
    String connectorId = properties.getConnectorId();
    Optional<ConnectorDefinition> connectorOpt =
        declarationsRegistry.flatMap(registry -> registry.getConnectorById(connectorId));
    if (connectorId != null && connectorOpt.isPresent()) {
      ConnectorDefinition connector = connectorOpt.get();
      if (!connector.getConnectorType().equals(type)) {
        throw SIPFrameworkException.init(
            "Connector id %s with wrong connector type (%s) used in %s. Use connector with type: (%s)",
            connectorId, connector.getConnectorType(), definitionPart, type);
      }
    }
  }
}
