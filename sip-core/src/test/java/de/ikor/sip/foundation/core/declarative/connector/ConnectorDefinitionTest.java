package de.ikor.sip.foundation.core.declarative.connector;

import java.lang.reflect.InvocationTargetException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConnectorDefinitionTest {

  @Test
  void GIVEN_connector_definition_none_entry_VERIFY_all_api_methods_tigger_error() {
    var instance = new ConnectorDefinition.None();
    var testMethods = instance.getClass().getDeclaredMethods();
    for (var method : testMethods) {
      Assertions.assertThatExceptionOfType(InvocationTargetException.class)
          .isThrownBy(() -> method.invoke(instance, null))
          .withCauseInstanceOf(UnsupportedOperationException.class);
    }
  }
}
