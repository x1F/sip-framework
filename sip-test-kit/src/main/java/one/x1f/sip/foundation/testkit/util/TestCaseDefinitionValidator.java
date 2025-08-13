package one.x1f.sip.foundation.testkit.util;

import static one.x1f.sip.foundation.testkit.config.TestCasesConfig.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.configurationproperties.TestCaseDefinition;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;

/** Methods for validating syntax of test case definitions. */
public class TestCaseDefinitionValidator {

  public static final String MISSING_TITLE_EXCEPTION_MSG = "Test title is missing";
  public static final String REQUIRED_WHEN_EXECUTE_EXCEPTION_MSG =
      "when-execute section is required for test: %s";
  public static final String MISSING_PARAMETERS_EXCEPTION_MSG =
      "Parameter endpointId or connectorId is missing in %s for test: %s (one of them should be specified)";
  public static final String BOTH_PARAMETERS_PROVIDED_EXCEPTION_MSG =
      "Both endpointId and connectorId parameters are defined in %s for test: %s (only one is allowed)";

  private TestCaseDefinitionValidator() {}

  public static void validate(TestCaseDefinition definition) {
    String testName = definition.getTitle();

    if (isEmpty(testName)) {
      throw new SIPFrameworkException(MISSING_TITLE_EXCEPTION_MSG);
    }

    if (definition.getWhenExecute() == null) {
      throw SIPFrameworkException.init(REQUIRED_WHEN_EXECUTE_EXCEPTION_MSG, testName);
    }

    validateEndpointIdAndConnectorIdFields(definition.getWhenExecute(), testName, WHEN_EXECUTE);
    definition
        .getWithMocks()
        .forEach(
            properties -> validateEndpointIdAndConnectorIdFields(properties, testName, WITH_MOCKS));
    definition
        .getThenExpect()
        .forEach(
            properties ->
                validateEndpointIdAndConnectorIdFields(properties, testName, THEN_EXPECT));
  }

  private static void validateEndpointIdAndConnectorIdFields(
      EndpointProperties properties, String testName, String definitionPart) {
    if (isEmpty(properties.getEndpointId()) && isEmpty(properties.getConnectorId())) {
      throw SIPFrameworkException.init(MISSING_PARAMETERS_EXCEPTION_MSG, definitionPart, testName);
    }
    if (isNotEmpty(properties.getEndpointId()) && isNotEmpty(properties.getConnectorId())) {
      throw SIPFrameworkException.init(
          BOTH_PARAMETERS_PROVIDED_EXCEPTION_MSG, definitionPart, testName);
    }
  }
}
