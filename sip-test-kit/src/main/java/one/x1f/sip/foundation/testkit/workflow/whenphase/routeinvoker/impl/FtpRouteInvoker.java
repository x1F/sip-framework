package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.headers.FtpExchangeHeaders.*;
import static org.apache.camel.Exchange.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.RouteInvokerException;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/** Invoker class for triggering routes with Ftp, Ftps, Sftp consumers */
@Component
@ConditionalOnClass(FtpComponent.class)
@RequiredArgsConstructor
public class FtpRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;

  @SuppressWarnings("unchecked")
  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    RemoteFileEndpoint<FTPFile> ftpEndpoint =
        (RemoteFileEndpoint<FTPFile>) resolveEndpoint(inputExchange, camelContext);
    RemoteFileConsumer<FTPFile> ftpConsumer =
        (RemoteFileConsumer<FTPFile>) resolveConsumer(inputExchange, camelContext);

    Exchange ftpExchange = ftpConsumer.createExchange(true);
    ftpExchange.getMessage().setBody(inputExchange.getMessage().getBody());
    ftpExchange
        .getMessage()
        .setHeaders(prepareFtpHeaders(ftpEndpoint.getConfiguration(), inputExchange));

    try {
      ftpConsumer.getProcessor().process(ftpExchange);
    } catch (Exception e) {
      throw new RouteInvokerException(this.getClass().getName());
    }
    return Optional.empty();
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof RemoteFileEndpoint;
  }

  @Override
  public boolean shouldSuspend(Endpoint endpoint) {
    return true;
  }

  private Map<String, Object> prepareFtpHeaders(
      RemoteFileConfiguration endpointConfiguration, Exchange inputExchange) {
    Map<String, Object> headers = inputExchange.getMessage().getHeaders();
    String endpointAbsolutePath =
        FileUtil.stripTrailingSeparator(endpointConfiguration.getDirectory());

    prepareDefaultHeaders(
        headers,
        endpointConfiguration,
        inputExchange.getMessage().getBody(String.class),
        endpointAbsolutePath);

    if (headers.containsKey(FILE_NAME)) {
      prepareFilenameHeaders(headers, endpointAbsolutePath);
    }
    prepareOtherHeaders(headers);
    return headers;
  }

  private void prepareDefaultHeaders(
      Map<String, Object> headers,
      RemoteFileConfiguration endpointConfiguration,
      String bodyPayload,
      String endpointAbsolutePath) {
    headers.putIfAbsent(
        CAMEL_FILE_ABSOLUTE.getValue(), FileUtil.hasLeadingSeparator(endpointAbsolutePath));
    headers.putIfAbsent(CAMEL_FILE_HOST.getValue(), endpointConfiguration.getHost());
    headers.putIfAbsent(FILE_LENGTH, (long) bodyPayload.length());
    headers.putIfAbsent(FILE_PARENT, FileUtil.stripLeadingSeparator(endpointAbsolutePath));
    if (endpointConfiguration.isStreamDownload()) {
      InputStream is = new ByteArrayInputStream(bodyPayload.getBytes());
      headers.putIfAbsent(RemoteFileComponent.REMOTE_FILE_INPUT_STREAM, is);
    }
  }

  private void prepareFilenameHeaders(Map<String, Object> headers, String endpointAbsolutePath) {
    String filename =
        normalizePathToProtocol(FileUtil.stripLeadingSeparator((String) headers.get(FILE_NAME)));
    headers.put(FILE_NAME, filename);
    headers.putIfAbsent(FILE_NAME_CONSUMED, filename);
    headers.putIfAbsent(FILE_NAME_ONLY, filename);
    headers.putIfAbsent(CAMEL_FILE_RELATIVE_PATH.getValue(), filename);
    headers.putIfAbsent(
        CAMEL_FILE_ABSOLUTE_PATH.getValue(),
        FileUtil.stripLeadingSeparator(endpointAbsolutePath + "/" + filename));
    headers.putIfAbsent(
        FILE_PATH, FileUtil.stripLeadingSeparator(endpointAbsolutePath) + "/" + filename);
  }

  private void prepareOtherHeaders(Map<String, Object> headers) {
    if (headers.containsKey(FILE_LAST_MODIFIED)) {
      Long lastModifiedTimestamp = (Long) headers.get(FILE_LAST_MODIFIED);
      headers.putIfAbsent(MESSAGE_TIMESTAMP, lastModifiedTimestamp);
    }
  }

  private String normalizePathToProtocol(String path) {
    if (!ObjectHelper.isEmpty(path)) {
      path = path.replace('/', File.separatorChar);
      path = path.replace('\\', File.separatorChar);
    }
    return path;
  }
}
