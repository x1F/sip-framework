package de.ikor.sip.foundation.core.actuator.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import java.util.Collections;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class AdapterRouteEndpointTest {

  private static final String ROUTE_ID = "test";
  private static final String SEDA_ROUTE_URI = "seda:test";

  private CamelContext camelContext;
  private AdapterRouteEndpoint subject;
  private ManagedCamelContext managedCamelContext;
  private ManagedRouteMBean managedRoute;
  private RoutesRegistry routesRegistry;

  @BeforeEach
  void setUp() {
    RouteControllerLoggingDecorator routeControllerLoggingDecorator;
    routeControllerLoggingDecorator =
        mock(RouteControllerLoggingDecorator.class, CALLS_REAL_METHODS);
    camelContext = mock(CamelContext.class, RETURNS_DEEP_STUBS);
    when(camelContext.getRoute(anyString()).getEndpoint().getEndpointUri()).thenReturn("");
    managedCamelContext = mock(ManagedCamelContext.class, RETURNS_DEEP_STUBS);
    ReflectionTestUtils.setField(routeControllerLoggingDecorator, "ctx", camelContext);
    routesRegistry = mock(RoutesRegistry.class, RETURNS_DEEP_STUBS);
    subject =
        new AdapterRouteEndpoint(
            camelContext, routeControllerLoggingDecorator, Optional.of(routesRegistry));
  }

  @Test
  void When_GettingRoute_Expect_RouteDetails() {
    // arrange
    mockRoutesInContext();

    // act
    AdapterRouteDetails adapterRouteDetails = subject.route(ROUTE_ID);

    // assert
    assertThat(adapterRouteDetails.getId()).isEqualTo(ROUTE_ID);
  }

  @Test
  void When_GettingRoutes_Expect_RoutesNotEmpty() {
    // arrange
    mockManagedRoute();
    Route route = getMockedRoute();
    when(camelContext.getRoutes()).thenReturn(Collections.singletonList(route));
    ReflectionTestUtils.setField(subject, "mbeanContext", managedCamelContext);

    // assert
    assertThat(subject.routes(null)).isNotEmpty();
  }

  @Test
  void When_ExecutingInvalidOperationOnRoute_Expect_IncompatibleOperationExceptionThrown() {
    // assert
    assertThatThrownBy(() -> subject.execute("", "none"))
        .isInstanceOf(IncompatibleOperationException.class);
  }

  @Test
  void When_ExecutingOperationsOnRoute_Expect_getRouteControllerCalledForEachOperation() {
    // act
    subject.execute(ROUTE_ID, "stop");
    subject.execute(ROUTE_ID, "start");
    subject.execute(ROUTE_ID, "suspend");
    subject.execute(ROUTE_ID, "resume");

    // assert
    verify(camelContext, times(4)).getRouteController();
  }

  @ParameterizedTest
  @ValueSource(strings = {"resume", "start", "stop", "suspend"})
  void When_OperationOnAllRoutes_Expect_CamelContextToCall_getRoutes_And_getRouteController(
      String operation) {
    // arrange
    mockRoutesInContext();
    // act
    subject.execute("all", operation);

    // assert
    verify(camelContext, times(1)).getRoutes();
    verify(camelContext, times(1)).getRouteController();
  }

  @Test
  void
      When_ResettingAllRoutes_Expect_CamelContextToCall_getRoutes_and_ManagedCamelContext_getManagedRoute() {
    // arrange
    mockRoutesInContext();
    // act
    subject.execute("all", "reset");

    // assert
    verify(camelContext, times(1)).getRoutes();
    verify(managedCamelContext, times(1)).getManagedRoute(ROUTE_ID);
  }

  @Test
  void When_ResettingRoute_Expect_MBeanContext_getManagedRoute() {
    // arrange
    mockRoutesInContext();
    // act
    subject.execute(ROUTE_ID, "reset");

    // assert
    verify(managedCamelContext, times(1)).getManagedRoute(ROUTE_ID);
  }

  private Route getMockedRoute() {
    Route route = mock(Route.class);
    when(route.getRouteId()).thenReturn(ROUTE_ID);
    return route;
  }

  private void mockManagedRoute() {
    managedRoute = mock(ManagedRouteMBean.class);
    when(managedRoute.getRouteId()).thenReturn(ROUTE_ID);
    when(managedRoute.getState()).thenReturn("test");
    when(managedRoute.getExchangesTotal()).thenReturn((long) 0);
    when(managedRoute.getExchangesCompleted()).thenReturn((long) 0);
    when(managedRoute.getExchangesFailed()).thenReturn((long) 0);
    when(managedRoute.getExchangesInflight()).thenReturn((long) 0);
    when(managedCamelContext.getManagedRoute(anyString())).thenReturn(managedRoute);
    when(camelContext.getCamelContextExtension().getContextPlugin(any()))
        .thenReturn(managedCamelContext);
  }

  private void mockRoutesInContext() {
    mockManagedRoute();
    Route route = getMockedRoute();
    Endpoint mockEndpoint = mock(Endpoint.class);
    when(route.getEndpoint()).thenReturn(mockEndpoint);
    when(mockEndpoint.getEndpointUri()).thenReturn(SEDA_ROUTE_URI);
    when(camelContext.getRoutes()).thenReturn(Collections.singletonList(route));
    ReflectionTestUtils.setField(subject, "mbeanContext", managedCamelContext);
  }
}
