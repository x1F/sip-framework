package one.x1f.sip.foundation.testkit.workflow.givenphase;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.givenphase.Mock.ENDPOINT_ID_EXCHANGE_PROPERTY;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvoker.CONNECTOR_ID_EXCHANGE_PROPERTY;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvokerTest.JSON_MODEL_PAYLOAD_BODY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.core.proxies.ProcessorProxyRegistry;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.exception.TestCaseInitializationException;
import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvokerTest;
import org.apache.camel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProcessorProxyMockTest {

  private static final String PROXY_ID = "id";
  private static final String PROXY_FIELD_NAME = "proxy";
  private static final String CONNECTOR_ID = "connectorId";

  private static final String STRING_BODY = "string value";
  private ProcessorProxyRegistry proxyRegistry;

  @BeforeEach
  void setup() {
    proxyRegistry = mock(ProcessorProxyRegistry.class);
  }

  @Nested
  class ProcessorProxyMockOnly {

    private ProcessorProxyMock subject;
    private ProcessorProxy proxy;

    @BeforeEach
    void setup() {
      proxy = mock(ProcessorProxy.class);
      Exchange returnExchange = mock(Exchange.class, RETURNS_DEEP_STUBS);
      subject = new ProcessorProxyMock(proxyRegistry, mock(ObjectMapper.class), null);
      subject.setReturnExchange(returnExchange);
      when(returnExchange.getProperty("connectionAlias", String.class)).thenReturn(PROXY_ID);
      when(returnExchange.getMessage().getBody()).thenReturn("body");
    }

    @Test
    void When_setBehavior_With_ExistingProxy_Expect_ProxySet() {
      // arrange
      when(proxyRegistry.getProxy(PROXY_ID)).thenReturn(Optional.of(proxy));

      // act
      subject.setBehavior(new TestExecutionStatus());

      // assert
      assertThat(ReflectionTestUtils.getField(subject, PROXY_FIELD_NAME)).isEqualTo(proxy);
    }

    @Test
    void When_setBehavior_With_ProxyMissing_Then_TestCaseInitializationException() {
      // act + assert
      TestExecutionStatus testExecutionStatus = new TestExecutionStatus();
      assertThatThrownBy(() -> subject.setBehavior(testExecutionStatus))
          .isInstanceOf(TestCaseInitializationException.class);
    }

    @Test
    void When_clear_With_SetProxy_Then_ProxyReset() {
      // arrange
      ReflectionTestUtils.setField(subject, PROXY_FIELD_NAME, proxy);

      // act
      subject.clear();

      // assert
      verify(proxy, times(1)).reset();
      verify(proxy, times(1)).mock(any());
    }

    @Test
    void When_clear_With_NullProxy_Expect_NoException() {
      // act + assert
      assertThatNoException().isThrownBy(() -> subject.clear());
      assertThat(ReflectionTestUtils.getField(subject, PROXY_FIELD_NAME)).isNull();
    }
  }

  @Nested
  class ProcessorProxyMockWithProcessorProxyAsSubject {

    private ProcessorProxy proxySubject;
    private DeclarationsRegistryApi declarationsRegistry;
    private Exchange actualExchange;

    @BeforeEach
    void setup() {
      declarationsRegistry = mock(DeclarationsRegistry.class);
      proxySubject =
          new ProcessorProxy(mock(NamedNode.class), mock(Processor.class), new ArrayList<>());
      ProcessorProxyMock processorProxyMock =
          new ProcessorProxyMock(
              proxyRegistry, new ObjectMapper(), Optional.of(declarationsRegistry));

      when(proxyRegistry.getProxy(PROXY_ID)).thenReturn(Optional.of(proxySubject));
      CamelContext camelContext = mock(CamelContext.class);
      when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));

      actualExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
      actualExchange.setProperty(ENDPOINT_ID_EXCHANGE_PROPERTY, PROXY_ID);
      actualExchange.setProperty(TEST_MODE_HEADER, "true");

      processorProxyMock.setReturnExchange(actualExchange);
      processorProxyMock.setBehavior(null);
    }

    @Test
    void
        GIVEN_stringAsExchangeBody_WHEN_processorProxyProcess_Expect_sameBodyWithoutUnmarshalling() {
      // arrange
      actualExchange.getMessage().setBody(STRING_BODY);

      // act
      proxySubject.process(actualExchange, mock(AsyncCallback.class));

      // assert
      assertThat(actualExchange.getMessage().getBody()).isEqualTo(STRING_BODY);
    }

    @Test
    void GIVEN_jsonStringAsExchangeBody_WHEN_processorProxyProcess_Expect_jsonPojoObject() {
      // arrange
      actualExchange.setProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, CONNECTOR_ID);
      actualExchange.getMessage().setBody(JSON_MODEL_PAYLOAD_BODY);

      DirectRouteInvokerTest.ConnectorMock connector =
          mock(DirectRouteInvokerTest.ConnectorMock.class);
      when(declarationsRegistry.getConnectorById(CONNECTOR_ID)).thenReturn(Optional.of(connector));
      doReturn(Optional.of(DirectRouteInvokerTest.Person.class))
          .when(connector)
          .getResponseModelClass();

      // act
      proxySubject.process(actualExchange, mock(AsyncCallback.class));

      // assert
      assertThat(actualExchange.getMessage().getBody())
          .isInstanceOf(DirectRouteInvokerTest.Person.class);
    }

    @Test
    void
        GIVEN_missingConnectorInDeclarationsRegistry_WHEN_processorProxyProcess_Expect_sameBodyWithoutUnmarshalling() {
      // arrange
      actualExchange.setProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, CONNECTOR_ID);
      actualExchange.getMessage().setBody(STRING_BODY);

      when(declarationsRegistry.getConnectorById(CONNECTOR_ID)).thenReturn(Optional.empty());

      // act
      proxySubject.process(actualExchange, mock(AsyncCallback.class));

      // assert
      assertThat(actualExchange.getMessage().getBody()).isEqualTo(STRING_BODY);
    }

    @Test
    void GIVEN_NoResponseModel_WHEN_processorProxyProcess_Expect_SIPFrameworkException() {
      // arrange
      actualExchange.setProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, CONNECTOR_ID);
      actualExchange.getMessage().setBody(JSON_MODEL_PAYLOAD_BODY);

      DirectRouteInvokerTest.ConnectorMock connector =
          mock(DirectRouteInvokerTest.ConnectorMock.class);
      when(declarationsRegistry.getConnectorById(CONNECTOR_ID)).thenReturn(Optional.of(connector));

      // act & assert
      assertThatThrownBy(
              () -> {
                proxySubject.process(actualExchange, mock(AsyncCallback.class));
              })
          .isInstanceOf(SIPFrameworkException.class)
          .hasMessage(
              String.format("Response model class is not defined for connector: %s", CONNECTOR_ID));
    }
  }
}
