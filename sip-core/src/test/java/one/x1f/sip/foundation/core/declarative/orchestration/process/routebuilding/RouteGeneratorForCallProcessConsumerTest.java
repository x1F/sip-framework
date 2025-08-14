package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallProcessConsumer;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.DSLTestHelper;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.RoutesDefinition;
import org.junit.jupiter.api.Test;

class RouteGeneratorForCallProcessConsumerTest {

  @Test
  void When_NoConsumersPresent_Expect_SIPFrameworkInitializationException()
      throws NoSuchMethodException, IllegalAccessException {
    // arrange
    CompositeProcessOrchestrationInfo orchestrationInfo =
        mock(CompositeProcessOrchestrationInfo.class, RETURNS_DEEP_STUBS);
    RouteGeneratorForCallProcessConsumer generator =
        new RouteGeneratorForCallProcessConsumer(orchestrationInfo, null, null);
    Method initSomethingMethod =
        RouteGeneratorForCallProcessConsumer.class.getDeclaredMethod(
            "retrieveConsumerFromClassDefinition", CallProcessConsumer.class);
    initSomethingMethod.setAccessible(true);
    RoutesDefinition routesDefinition = mock(RoutesDefinition.class, RETURNS_DEEP_STUBS);
    when(orchestrationInfo.getRoutesDefinition()).thenReturn(routesDefinition);
    when(routesDefinition
            .getCamelContext()
            .getRegistry()
            .findSingleByType(DeclarationsRegistryApi.class))
        .thenReturn(mock(DeclarationsRegistry.class));
    CallProcessConsumer callProcessConsumer = DSLTestHelper.initCallProcessConsumer();

    try {
      // act
      initSomethingMethod.invoke(generator, callProcessConsumer);
    } catch (InvocationTargetException e) {
      // assert
      assertThat(e.getTargetException()).isInstanceOf(SIPFrameworkInitializationException.class);
      assertThat(e.getTargetException().getMessage())
          .contains("Consumer-class ", " is used on orchestration");
    }
  }
}
