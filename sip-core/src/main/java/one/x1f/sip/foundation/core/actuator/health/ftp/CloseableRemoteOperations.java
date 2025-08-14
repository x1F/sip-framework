package one.x1f.sip.foundation.core.actuator.health.ftp;

import java.util.function.BiConsumer;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileOperations;

class CloseableRemoteOperations implements AutoCloseable {
  private final RemoteFileEndpoint<?> endpoint;
  private final RemoteFileOperations<?> fileOperations;

  CloseableRemoteOperations(
      RemoteFileEndpoint<?> endpoint, RemoteFileOperations<?> fileOperations) {
    this.endpoint = endpoint;
    this.fileOperations = fileOperations;
  }

  void doHealthCheck(BiConsumer<RemoteFileEndpoint<?>, RemoteFileOperations<?>> healthCheck) {
    healthCheck.accept(endpoint, fileOperations);
  }

  @Override
  public void close() {
    if (endpoint.isDisconnect()) {
      fileOperations.disconnect();
    }
  }
}
