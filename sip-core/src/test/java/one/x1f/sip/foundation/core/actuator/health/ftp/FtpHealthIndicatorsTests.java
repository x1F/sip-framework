package one.x1f.sip.foundation.core.actuator.health.ftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class FtpHealthIndicatorsTests {

  private RemoteFileEndpoint endpoint;

  @BeforeEach
  void setUp() {
    endpoint = mock(RemoteFileEndpoint.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void Given_NoneRemoteFileEndpoint_When_noopHealthIndicator_Then_IntegrationManagementException() {
    // assert
    assertThatThrownBy(() -> FtpHealthIndicators.noopHealthIndicator(mock(Endpoint.class)))
        .isInstanceOf(IntegrationManagementException.class);
  }

  @Test
  void
      Given_CreateRemoteFileOperationsThrowsException_When_noopHealthIndicatorAnd_Then_statusUnknown()
          throws Exception {
    // act
    when(endpoint.createRemoteFileOperations()).thenThrow(new Exception());

    // assert
    assertThat(FtpHealthIndicators.noopHealthIndicator(endpoint).getStatus())
        .isEqualTo(Status.UNKNOWN);
  }

  @Test
  void Given_OperationNotConnected_When_noopHealthIndicator_Then_statusUp() throws Exception {
    // arrange
    RemoteFileOperations operations = mock(RemoteFileOperations.class);
    when(endpoint.createRemoteFileOperations()).thenReturn(operations);
    when(operations.isConnected()).thenReturn(false);

    // act
    Status subjectStatus = FtpHealthIndicators.noopHealthIndicator(endpoint).getStatus();

    // assert
    assertThat(subjectStatus).isEqualTo(Status.UP);
    verify(operations, times(2)).connect(any(), any());
  }

  @Test
  void Given_OperationConnected_When_noopHealthIndicator_Then_statusUp() throws Exception {
    // arrange
    RemoteFileOperations operations = mock(RemoteFileOperations.class);
    when(endpoint.createRemoteFileOperations()).thenReturn(operations);
    when(operations.isConnected()).thenReturn(true);
    when(operations.connect(any(), any())).thenReturn(true);

    // act
    Status subjectStatus = FtpHealthIndicators.noopHealthIndicator(endpoint).getStatus();

    // assert
    assertThat(subjectStatus).isEqualTo(Status.UP);
    verify(operations, times(1)).connect(any(), any());
  }
}
