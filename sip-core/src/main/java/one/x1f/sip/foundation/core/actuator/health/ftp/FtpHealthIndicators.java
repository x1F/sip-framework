package one.x1f.sip.foundation.core.actuator.health.ftp;

import static one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper.appendMetadata;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.file.remote.RemoteFileEndpoint;
import org.apache.camel.component.file.remote.RemoteFileOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;

/**
 * Class {@link FtpHealthIndicators} provides a set of utility functions that can perform operations
 * on remote FTP servers to check if they are healthy.
 *
 * <p>The criteria of healthy can vary: is it the ability to list files from a directory on a remote
 * server, or we simply want to be able to execute the NOOP FTP request (the default health
 * indicator).
 */
public class FtpHealthIndicators {
  private static final Logger logger = LoggerFactory.getLogger(FtpHealthIndicators.class);

  private FtpHealthIndicators() {}

  /**
   * Executes the FTP NOOP request to check if the remote FTP server is healthy.
   *
   * @param endpoint Remote FTP endpoint
   * @return Health status based on application ability to execute FTP NOOP request against the
   *     remote server
   */
  public static Health noopHealthIndicator(Endpoint endpoint) {
    return inspectHealth(endpoint, FtpHealthConsumers::noopConsumer);
  }

  /**
   * Lists folder from defined in endpoint URI as a health check operation. If folder listing
   * completes with no error, endpoint is considered healthy.
   *
   * @param endpoint Remote FTP endpoint
   * @return Health status based on application ability to execute FTP listDir request against the
   *     remote server
   */
  public static Health listDirHealthIndicator(Endpoint endpoint) {
    return inspectHealth(endpoint, FtpHealthConsumers::listDirectoryConsumer);
  }

  /**
   * A factory for health indicator functions that gives the possibility to define a custom health
   * check and use it in the SIP health checking framework.
   *
   * @param healthCheck A function implementing criteria for "healthy"
   * @return Returns a health indicator: function that evaluates if an endpoint is healthy
   * @see FtpHealthConsumers#listDirectoryConsumer(RemoteFileEndpoint, RemoteFileOperations)
   * @see FtpHealthConsumers#noopConsumer(RemoteFileEndpoint, RemoteFileOperations)
   */
  public static Function<Endpoint, Health> ftpHealthIndicator(
      BiConsumer<RemoteFileEndpoint<?>, RemoteFileOperations<?>> healthCheck) {
    return endpoint -> inspectHealth(endpoint, healthCheck);
  }

  /**
   * Returns {@link Health} information based on the ability to execute the healthCheck function
   *
   * @param endpoint the FTP endpoint
   * @return Returns health status UP if it can execute the healthCheck function, or DOWN along with
   *     the exception message otherwise.
   */
  private static Health inspectHealth(
      Endpoint endpoint, BiConsumer<RemoteFileEndpoint<?>, RemoteFileOperations<?>> healthCheck) {
    RemoteFileEndpoint<?> remoteFileEndpoint = validateEndpointType(endpoint);
    RemoteFileOperations<?> remoteFileOperations;
    try {
      remoteFileOperations = remoteFileEndpoint.createRemoteFileOperations();
    } catch (Exception e) {
      return new Health.Builder().unknown().withException(e).build();
    }

    return doInspectHealth(remoteFileEndpoint, remoteFileOperations, healthCheck);
  }

  private static Health doInspectHealth(
      RemoteFileEndpoint<?> remoteFileEndpoint,
      RemoteFileOperations<?> remoteFileOperations,
      BiConsumer<RemoteFileEndpoint<?>, RemoteFileOperations<?>> healthCheck) {
    final Health.Builder builder = new Health.Builder();
    builder.withDetails(appendMetadata(remoteFileEndpoint, new HashMap<>()));
    try (CloseableRemoteOperations remoteOps =
        new CloseableRemoteOperations(remoteFileEndpoint, remoteFileOperations)) {
      connectIfNotConnected(remoteFileEndpoint, remoteFileOperations);
      remoteOps.doHealthCheck(healthCheck);
      builder.up();
    } catch (Exception ex) {
      logger.warn("Endpoint {} is not healthy: {}", remoteFileEndpoint, ex.getMessage());
      builder.down(ex);
    }
    return builder.build();
  }

  private static void connectIfNotConnected(
      RemoteFileEndpoint<?> remoteFileEndpoint, RemoteFileOperations<?> remoteFileOperations) {
    if (!remoteFileOperations.isConnected()) {
      remoteFileOperations.connect(remoteFileEndpoint.getConfiguration(), null);
      logger.debug("Connected and authenticated to {}", remoteFileEndpoint);
    }
  }

  private static RemoteFileEndpoint<?> validateEndpointType(Endpoint endpoint) {
    if (!(endpoint instanceof RemoteFileEndpoint)) {
      throw new IntegrationManagementException(
          "Invalid endpoint health configuration: "
              + "endpoint "
              + endpoint
              + " is not an instance of the RemoteFileEndpoint: "
              + endpoint.getClass());
    }

    return (RemoteFileEndpoint<?>) endpoint;
  }
}
