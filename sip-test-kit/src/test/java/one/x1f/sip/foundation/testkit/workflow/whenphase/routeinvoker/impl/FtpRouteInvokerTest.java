package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.headers.FtpExchangeHeaders.*;
import static org.apache.camel.Exchange.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.RouteInvokerException;
import org.apache.camel.*;
import org.apache.camel.component.file.remote.RemoteFileComponent;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.camel.component.file.remote.RemoteFileConsumer;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FtpRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String BODY_PAYLOAD = "Testing body";
  private static final String NO_HEADER = "no header";
  private static final String TESTING_FILE_NAME = "testingFile.txt";
  private static final String TESTING_DIRECTORY = "test/directory";

  private FtpRouteInvoker subject;
  private Exchange inputExchange;
  private Processor processor;
  private Exchange actualFileExchange;
  private RemoteFileEndpoint<FTPFile> ftpEndpoint;
  private RemoteFileConfiguration endpointConfiguration;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    subject = new FtpRouteInvoker(camelContext);
    inputExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    inputExchange.setProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);
    inputExchange.getMessage().setBody(BODY_PAYLOAD);

    Route route = mock(Route.class);
    RemoteFileConsumer<FTPFile> ftpConsumer = mock(RemoteFileConsumer.class);
    processor = mock(Processor.class);
    ftpEndpoint = mock(RemoteFileEndpoint.class);

    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getEndpoint()).thenReturn(ftpEndpoint);
    when(route.getConsumer()).thenReturn(ftpConsumer);
    when(ftpConsumer.getProcessor()).thenReturn(processor);

    actualFileExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    when(ftpConsumer.createExchange(true)).thenReturn(actualFileExchange);

    endpointConfiguration = mock(RemoteFileConfiguration.class);
    when(ftpEndpoint.getConfiguration()).thenReturn(endpointConfiguration);
    when(endpointConfiguration.getDirectory()).thenReturn(TESTING_DIRECTORY);
    when(endpointConfiguration.getHost()).thenReturn("localhost");
  }

  @ParameterizedTest(name = "Using input header CamelFileLength: {0}")
  @ValueSource(strings = {NO_HEADER, "15"})
  void
      GIVEN_defaultHeadersOrFileLengthHeader_WHEN_invoke_THEN_verifyProcessorCallAndDefaultHeadersAndBody(
          String lengthValue) throws Exception {
    // arrange
    if (isHeaderProvidedInConfiguration(lengthValue)) {
      inputExchange.getMessage().setHeader(FILE_LENGTH, Long.valueOf(lengthValue));
    }

    // act
    subject.invoke(inputExchange);

    // assert
    verify(processor, times(1)).process(actualFileExchange);
    assertThat(actualFileExchange.getMessage().getBody()).isEqualTo(BODY_PAYLOAD);
    assertThat(actualFileExchange.getMessage().getHeader(CAMEL_FILE_HOST.getValue()))
        .isEqualTo("localhost");
    assertThat(
            actualFileExchange
                .getMessage()
                .getHeader(CAMEL_FILE_ABSOLUTE.getValue(), Boolean.class))
        .isFalse();
    assertThat(actualFileExchange.getMessage().getHeader(FILE_PARENT)).isEqualTo(TESTING_DIRECTORY);
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

  @ParameterizedTest(name = "Using input header CamelFileRelativePath: {0}")
  @ValueSource(strings = {NO_HEADER, "inputFile.txt"})
  void GIVEN_filenameHeader_WHEN_invoke_THEN_getCamelFileRelativePathHeader(
      String camelFileRelativePath) {
    // arrange
    inputExchange.getMessage().setHeader(FILE_NAME, TESTING_FILE_NAME);
    if (isHeaderProvidedInConfiguration(camelFileRelativePath)) {
      inputExchange
          .getMessage()
          .setHeader(CAMEL_FILE_RELATIVE_PATH.getValue(), camelFileRelativePath);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(CAMEL_FILE_RELATIVE_PATH.getValue()))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelFileRelativePath)
                ? camelFileRelativePath
                : TESTING_FILE_NAME);
  }

  @ParameterizedTest(name = "Using input header CamelFileAbsolutePath: {0}")
  @ValueSource(strings = {NO_HEADER, "test/inputFile.txt"})
  void GIVEN_filenameHeader_WHEN_invoke_THEN_getCamelFileAbsolutePathHeader(
      String camelFileAbsolutePath) {
    // arrange
    inputExchange.getMessage().setHeader(FILE_NAME, TESTING_FILE_NAME);
    if (isHeaderProvidedInConfiguration(camelFileAbsolutePath)) {
      inputExchange
          .getMessage()
          .setHeader(CAMEL_FILE_ABSOLUTE_PATH.getValue(), camelFileAbsolutePath);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(CAMEL_FILE_ABSOLUTE_PATH.getValue()))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelFileAbsolutePath)
                ? camelFileAbsolutePath
                : TESTING_DIRECTORY + "/" + TESTING_FILE_NAME);
  }

  @ParameterizedTest(name = "Using input header CamelFilePath: {0}")
  @ValueSource(strings = {NO_HEADER, "test/inputFile.txt"})
  void GIVEN_filenameHeader_WHEN_invoke_THEN_getCamelFilePathHeader(String camelFilePath) {
    // arrange
    inputExchange.getMessage().setHeader(FILE_NAME, TESTING_FILE_NAME);
    if (isHeaderProvidedInConfiguration(camelFilePath)) {
      inputExchange.getMessage().setHeader(FILE_PATH, camelFilePath);
    }

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualFileExchange.getMessage().getHeader(FILE_PATH))
        .isEqualTo(
            isHeaderProvidedInConfiguration(camelFilePath)
                ? camelFilePath
                : TESTING_DIRECTORY + "/" + TESTING_FILE_NAME);
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

  @DisplayName("Using input header CamelRemoteFileInputStream")
  @Test
  void GIVEN_streamDownloadTrue_WHEN_invoke_THEN_getCamelRemoteFileInputStreamHeader() {
    // arrange
    when(endpointConfiguration.isStreamDownload()).thenReturn(true);
    inputExchange
        .getMessage()
        .setHeader(RemoteFileComponent.REMOTE_FILE_INPUT_STREAM, "stream value");

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(
            actualFileExchange.getMessage().getHeader(RemoteFileComponent.REMOTE_FILE_INPUT_STREAM))
        .isEqualTo("stream value");
  }

  @Test
  void GIVEN_simulatedException_WHEN_invoke_THEN_expectRouteInvokerRuntimeException()
      throws Exception {
    // arrange
    doThrow(Exception.class).when(processor).process(any());

    // act & assert
    assertThrows(RouteInvokerException.class, () -> subject.invoke(inputExchange));
  }

  @Test
  void GIVEN_remoteFileEndpoint_WHEN_isApplicable_THEN_returnTrue() {
    // act & assert
    assertThat(subject.isApplicable(ftpEndpoint)).isTrue();
  }

  @Test
  void GIVEN_noRemoteFileEndpoint_WHEN_isApplicable_THEN_returnTrue() {
    // arrange
    Endpoint endpoint = mock(Endpoint.class);

    // act & assert
    assertThat(subject.isApplicable(endpoint)).isFalse();
  }

  private boolean isHeaderProvidedInConfiguration(String headerValue) {
    return !headerValue.equals(NO_HEADER);
  }
}
