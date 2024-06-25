package de.ikor.sip.foundation.core.declarative.connector;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ConnectorDefinitionTest {

  @Test
  void GIVEN_connector_definition_none_entry_VERIFY_all_api_methods_tigger_error()
      throws InvocationTargetException, IllegalAccessException {
    var instance = new ConnectorDefinition.None();
    var testMethods = instance.getClass().getDeclaredMethods();
    for (var method : testMethods) {
      var args = Arrays.stream(method.getParameterTypes()).map(Mockito::mock).toList();
      Assertions.assertThatExceptionOfType(InvocationTargetException.class)
          .isThrownBy(() -> method.invoke(instance, args.toArray()))
          .withCauseInstanceOf(UnsupportedOperationException.class);
    }
  }
}
