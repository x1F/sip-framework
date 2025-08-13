package one.x1f.sip.foundation.core.actuator.health.ftp;

import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class collect different Bi-consumer static methods, taking endpoint and RemoteFileOperations.
 * Those bi-consumers are used as part of health check functionalities. If they execute without
 * error, endpoint parameter is considered healthy.
 */
public class FtpHealthConsumers {
  private static final Logger logger = LoggerFactory.getLogger(FtpHealthConsumers.class);

  private FtpHealthConsumers() {}

  /**
   * Executes the FTP NOOP request to check if the remote FTP server is healthy.
   *
   * @param endpoint Remote FTP endpoint
   * @param fileOps {@link RemoteFileOperations} managed by the Camel FTP component
   */
  static void noopConsumer(RemoteFileEndpoint<?> endpoint, RemoteFileOperations<?> fileOps) {
    boolean noopSucceeded = false;
    try {
      noopSucceeded = fileOps.sendNoop();
    } catch (Exception e) {
      logger.debug("Failed to sendNoop to {}: {}", endpoint, e.getMessage());
    }

    if (!noopSucceeded && (fileOps.connect(endpoint.getConfiguration(), null))) {
      logger.debug("Connected and authenticated to: {}", endpoint);
    }
  }

  /**
   * Returns health information based on the application ability to list files of the directory from
   * the endpoint URI
   *
   * @param endpoint Remote FTP endpoint
   * @param fileOps {@link RemoteFileOperations} managed by the Camel FTP component
   */
  static void listDirectoryConsumer(
      RemoteFileEndpoint<?> endpoint, RemoteFileOperations<?> fileOps) {
    String directoryName = endpoint.getConfiguration().getDirectoryName();
    if (!fileOps.existsFile(directoryName)) {
      throw new IntegrationManagementException("Folder " + directoryName + "not found");
    }

    fileOps.listFiles(directoryName);
  }
}
