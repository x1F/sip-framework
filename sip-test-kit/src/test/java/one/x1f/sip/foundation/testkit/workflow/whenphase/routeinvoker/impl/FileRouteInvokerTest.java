package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static org.apache.camel.Exchange.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import org.apache.camel.*;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.support.EmptyAsyncCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FileRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String BODY_PAYLOAD = "Testing body";
  private static final String NO_HEADER = "no header";
  private static final String TESTING_FILE_NAME = "testingFile.txt";

  private FileRouteInvoker subject;
  private Exchange inputExchange;
  private AsyncProcessor asyncProcessor;
  private Exchange actualFileExchange;

  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    subject = new FileRouteInvoker(camelContext);
    inputExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    inputExchange.setProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);
    inputExchange.getMessage().setBody(BODY_PAYLOAD);

    Route route = mock(Route.class);
    FileConsumer fileConsumer = mock(FileConsumer.class);
    asyncProcessor = mock(AsyncProcessor.class);

    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getConsumer()).thenReturn(fileConsumer);
    when(fileConsumer.getAsyncProcessor()).thenReturn(asyncProcessor);

    actualFileExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    when(fileConsumer.createExchange(true)).thenReturn(actualFileExchange);
  }

  @ParameterizedTest(name = "Using input header CamelFileLength: {0}")
  @ValueSource(strings = {NO_HEADER, "15"})
  void
      GIVEN_onlyTestKitPreparedExchangeByDefault_WHEN_invoke_THEN_verifyProcessorCallAndDefaultHeadersAndBody(
          String lengthValue) {
    // arrange
    if (isHeaderProvidedInConfiguration(lengthValue)) {
      inputExchange.getMessage().setHeader(FILE_LENGTH, Long.valueOf(lengthValue));
    }

    // act
    subject.invoke(inputExchange);

    // assert
    verify(asyncProcessor, times(1)).process(actualFileExchange, EmptyAsyncCallback.get());
    assertThat(actualFileExchange.getMessage().getBody()).isEqualTo(BODY_PAYLOAD);
    assertThat(actualFileExchange.getMessage().getHeader(FILE_LENGTH))
        .isEqualTo(
            isHeaderProvidedInConfiguration(lengthValue)
                ? Long.parseLong(lengthValue)
                : (long) BODY_PAYLOAD.length());
  }

  @ParameterizedTest(name = "Using input header CamelFileNameConsumed: {0}")
  @ValueSource(strings = {NO_HEADER, "inputFile.txt"})
  void GIVEN_filenameHeader_WHEN_invoke_THEN_getCamelFileNameConsumedHeader(
      String camelFileNameConsumed) {
    // arrange
    inputExchange.getMessage().setHeader(FILE_NAME, TESTING_FILE_NAME);
    if (isHeaderProvidedInConfiguration(camelFileNameConsumed)) {
      inputExchange.getMessage().setHeader(FILE_NAME_CONSUMED, camelFileNameConsumed);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(FILE_NAME_CONSUMED))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelFileNameConsumed)
                ? camelFileNameConsumed
                : TESTING_FILE_NAME);
  }

  @ParameterizedTest(name = "Using input header CamelFileNameOnly: {0}")
  @ValueSource(strings = {NO_HEADER, "inputFile.txt"})
  void GIVEN_filenameHeader_WHEN_invoke_THEN_getCamelFileNameOnlyHeader(String camelFileNameOnly) {
    // arrange
    inputExchange.getMessage().setHeader(FILE_NAME, TESTING_FILE_NAME);
    if (isHeaderProvidedInConfiguration(camelFileNameOnly)) {
      inputExchange.getMessage().setHeader(FILE_NAME_ONLY, camelFileNameOnly);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(FILE_NAME_ONLY))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelFileNameOnly)
                ? camelFileNameOnly
                : TESTING_FILE_NAME);
  }

  @ParameterizedTest(name = "Using input header CamelMessageTimestamp: {0}")
  @ValueSource(strings = {NO_HEADER, "16589146810000"})
  void GIVEN_lastModifiedHeader_WHEN_invoke_THEN_getCamelMessageTimestampHeader(
      String camelMessageTimestamp) {
    // arrange
    Long lastModifiedTimestamp = Long.valueOf("1658914689700");
    inputExchange.getMessage().setHeader(FILE_LAST_MODIFIED, lastModifiedTimestamp);
    if (isHeaderProvidedInConfiguration(camelMessageTimestamp)) {
      inputExchange.getMessage().setHeader(MESSAGE_TIMESTAMP, camelMessageTimestamp);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(MESSAGE_TIMESTAMP))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelMessageTimestamp)
                ? camelMessageTimestamp
                : lastModifiedTimestamp);
  }

  private boolean isHeaderProvidedInConfiguration(String headerValue) {
    return !headerValue.equals(NO_HEADER);
  }
}
