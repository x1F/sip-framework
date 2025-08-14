package one.x1f.sip.foundation.core.actuator.health.ftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class FtpHealthConsumersTest {

  private ListAppender<ILoggingEvent> listAppender;
  private RemoteFileOperations fileOps;
  private RemoteFileEndpoint<?> endpoint;
  private RemoteFileConfiguration configuration;
  private List<ILoggingEvent> logsList;

  @BeforeEach
  void setUp() {
    fileOps = mock(RemoteFileOperations.class);
    endpoint = mock(RemoteFileEndpoint.class);
    configuration = mock(RemoteFileConfiguration.class);
    Logger logger = (Logger) LoggerFactory.getLogger(FtpHealthConsumers.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.setLevel(Level.DEBUG);
    logger.addAppender(listAppender);
    logsList = listAppender.list;
  }

  @Test
  void Given_SendNoopNotSuccessfulAndConnectSuccessful_When_noopConsumer_Then_connectedMessage() {
    // arrange
    when(endpoint.getConfiguration()).thenReturn(null);
    when(fileOps.sendNoop()).thenReturn(false);
    when(fileOps.connect(any(), any())).thenReturn(true);

    // act
    FtpHealthConsumers.noopConsumer(endpoint, fileOps);
    ILoggingEvent subject = logsList.get(0);

    // assert
    verify(fileOps, times(1)).connect(any(), any());
    assertThat(subject.getMessage()).isEqualTo("Connected and authenticated to: {}");
    assertThat(subject.getLevel()).isEqualTo(Level.DEBUG);
  }

  @Test
  void Given_SendNoopThrowsException_When_noopConsumer_Then_failMessage() {
    // arrange
    when(endpoint.getConfiguration()).thenReturn(null);
    when(fileOps.sendNoop()).thenThrow(new RuntimeException("msg"));
    when(fileOps.connect(any(), any())).thenReturn(true);

    // act
    FtpHealthConsumers.noopConsumer(endpoint, fileOps);
    ILoggingEvent subject = logsList.get(0);

    // assert
    assertThat(subject.getMessage()).isEqualTo("Failed to sendNoop to {}: {}");
    assertThat(subject.getLevel()).isEqualTo(Level.DEBUG);
  }

  @Test
  void Given_sendNoopSuccessful_When_executeNoop_Then_emptyLogList() {
    // arrange
    when(endpoint.getConfiguration()).thenReturn(null);
    when(fileOps.sendNoop()).thenReturn(true);
    when(fileOps.connect(any(), any())).thenReturn(true);

    // act
    FtpHealthConsumers.noopConsumer(endpoint, fileOps);

    // assert
    verify(fileOps, times(0)).connect(any(), any());
    assertThat(logsList).isEmpty();
  }

  @Test
  void Given_OperationConnectUnsuccessful_When_executeNoop_Then_emptyLogList() {
    // arrange
    when(endpoint.getConfiguration()).thenReturn(null);
    when(fileOps.sendNoop()).thenReturn(false);
    when(fileOps.connect(any(), any())).thenReturn(false);

    // act
    FtpHealthConsumers.noopConsumer(endpoint, fileOps);

    // assert
    verify(fileOps, times(1)).connect(any(), any());
    assertThat(logsList).isEmpty();
  }

  @Test
  void Given_StatusUp_When_executeListDir_Then_emptyLogList() {
    // arrange
    String directoryName = "dirName";
    when(configuration.getDirectoryName()).thenReturn(directoryName);
    when(endpoint.getConfiguration()).thenReturn(configuration);
    when(fileOps.existsFile(directoryName)).thenReturn(true);
    when(fileOps.listFiles(directoryName)).thenReturn(new Object[0]);

    // act
    FtpHealthConsumers.listDirectoryConsumer(endpoint, fileOps);

    // assert
    verify(fileOps, times(1)).listFiles(directoryName);
    assertThat(logsList).isEmpty();
  }

  @Test
  void Given_UnknownFolder_When_executeListDir_Then_folderNotFoundMessage() {
    // arrange
    String directoryName = "dirName";
    when(configuration.getDirectoryName()).thenReturn(directoryName);
    when(endpoint.getConfiguration()).thenReturn(configuration);
    when(fileOps.existsFile(directoryName)).thenReturn(false);

    // act
    Exception exceptionSubject =
        catchThrowableOfType(
            IntegrationManagementException.class,
            () -> FtpHealthConsumers.listDirectoryConsumer(endpoint, fileOps));

    // assert
    verify(fileOps, times(0)).listFiles(directoryName);
    assertThat(exceptionSubject.getMessage()).isEqualTo("Folder " + directoryName + "not found");
  }
}
