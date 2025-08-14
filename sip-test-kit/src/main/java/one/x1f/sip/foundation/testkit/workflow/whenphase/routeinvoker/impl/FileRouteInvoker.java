package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static org.apache.camel.Exchange.*;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.*;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.support.EmptyAsyncCallback;
import org.springframework.stereotype.Component;

/** Invoker class for triggering routes with File consumer */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;

  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    FileConsumer fileConsumer = (FileConsumer) resolveConsumer(inputExchange, camelContext);

    Exchange fileExchange = fileConsumer.createExchange(true);
    fileExchange.getMessage().setBody(inputExchange.getMessage().getBody());
    fileExchange.getMessage().setHeaders(prepareFileHeaders(inputExchange));

    fileConsumer.getAsyncProcessor().process(fileExchange, EmptyAsyncCallback.get());
    return Optional.empty();
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof FileEndpoint;
  }

  @Override
  public boolean shouldSuspend(Endpoint endpoint) {
    return true;
  }

  private Map<String, Object> prepareFileHeaders(Exchange inputExchange) {
    Map<String, Object> headers = inputExchange.getMessage().getHeaders();

    prepareDefaultHeaders(headers, inputExchange.getMessage().getBody(String.class));
    if (headers.containsKey(FILE_NAME)) {
      prepareFilenameHeaders(headers);
    }
    prepareOtherHeaders(headers);

    return headers;
  }

  private void prepareDefaultHeaders(Map<String, Object> headers, String bodyPayload) {
    headers.putIfAbsent(FILE_LENGTH, (long) bodyPayload.length());
  }

  private void prepareFilenameHeaders(Map<String, Object> headers) {
    String filename = (String) headers.get(FILE_NAME);
    headers.putIfAbsent(FILE_NAME_CONSUMED, filename);
    headers.putIfAbsent(FILE_NAME_ONLY, filename);
  }

  private void prepareOtherHeaders(Map<String, Object> headers) {
    if (headers.containsKey(FILE_LAST_MODIFIED)) {
      Long lastModifiedTimestamp = (Long) headers.get(FILE_LAST_MODIFIED);
      headers.putIfAbsent(MESSAGE_TIMESTAMP, lastModifiedTimestamp);
    }
  }
}
