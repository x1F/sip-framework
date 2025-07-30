package de.ikor.sip.foundation.core.declarative.connector;

import java.lang.reflect.InvocationTargetException;
import org.apache.camel.model.RouteDefinition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConnectorProcessorTest {

  @Test
  void GIVEN_connector_processor_none_entry_VERIFY_all_api_methods_tigger_error()
      throws NoSuchMethodException {
    var instance = new ConnectorExtension.None();
    var processMethod = instance.getClass().getDeclaredMethod("accept", RouteDefinition.class);
    var routeDef = Mockito.mock(RouteDefinition.class);
    Assertions.assertThatExceptionOfType(InvocationTargetException.class)
        .isThrownBy(() -> processMethod.invoke(instance, routeDef))
        .withCauseInstanceOf(UnsupportedOperationException.class);
  }
}
