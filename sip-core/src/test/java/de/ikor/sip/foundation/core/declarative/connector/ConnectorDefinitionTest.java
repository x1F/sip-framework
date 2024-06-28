package de.ikor.sip.foundation.core.declarative.connector;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

class ConnectorDefinitionTest {

  @Test
  void GIVEN_connector_definition_none_entry_VERIFY_all_api_methods_tigger_error() {
    var instance = new ConnectorDefinition.None();

    assertForUnsupportedOperationException(instance::getConnectorGroupId);
    assertForUnsupportedOperationException(instance::getConnectorType);
    assertForUnsupportedOperationException(instance::getId);
    assertForUnsupportedOperationException(instance::getOrchestrator);
    assertForUnsupportedOperationException(instance::getPathToDocumentationResource);
    assertForUnsupportedOperationException(instance::getRequestModelClass);
    assertForUnsupportedOperationException(instance::getResponseModelClass);
    assertForUnsupportedOperationException(instance::getScenarioId);
  }

  private <T> void assertForUnsupportedOperationException(ThrowableAssert.ThrowingCallable call) {
    Assertions.assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(call);
  }
}
