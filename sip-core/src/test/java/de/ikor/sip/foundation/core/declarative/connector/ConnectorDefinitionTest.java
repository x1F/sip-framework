package de.ikor.sip.foundation.core.declarative.connector;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

public class ConnectorDefinitionTest {

  @Test
  void GIVEN_connector_definition_none_entry_VERIFY_all_api_methods_tigger_error() {
    var instance = new ConnectorDefinition.None();

    assertForUnsupportedOperationException(instance, instance::getConnectorGroupId);
    assertForUnsupportedOperationException(instance, instance::getConnectorType);
    assertForUnsupportedOperationException(instance, instance::getId);
    assertForUnsupportedOperationException(instance, instance::getOrchestrator);
    assertForUnsupportedOperationException(instance, instance::getPathToDocumentationResource);
    assertForUnsupportedOperationException(instance, instance::getRequestModelClass);
    assertForUnsupportedOperationException(instance, instance::getResponseModelClass);
    assertForUnsupportedOperationException(instance, instance::getScenarioId);
  }

  private <T> void assertForUnsupportedOperationException(
      ConnectorDefinition def, ThrowableAssert.ThrowingCallable call) {
    Assertions.assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(call);
  }
}
