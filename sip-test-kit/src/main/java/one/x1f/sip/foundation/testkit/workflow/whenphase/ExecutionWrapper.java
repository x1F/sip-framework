package one.x1f.sip.foundation.testkit.workflow.whenphase;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.Exchange;

/** Executes WhenPhaseDefinition */
@Slf4j
@Data
@AllArgsConstructor
public class ExecutionWrapper {

  private String testName;
  private Exchange whenDefinitionExchange;
  private final RouteInvoker invoker;

  /**
   * WhenPhaseDefinition
   *
   * @return {@link Exchange}
   */
  public Optional<Exchange> execute() {
    log.info("sip.testkit.workflow.startcamelrequest");
    enrichWithTestHeaders();
    return invoker.invoke(whenDefinitionExchange);
  }

  private void enrichWithTestHeaders() {
    Map<String, Object> headers = whenDefinitionExchange.getMessage().getHeaders();
    headers.put(RouteInvoker.TEST_NAME_HEADER, testName);
    headers.put(ProcessorProxy.TEST_MODE_HEADER, true);
    Map<String, Object> properties = whenDefinitionExchange.getProperties();
    properties.put(RouteInvoker.TEST_NAME_HEADER, testName);
    properties.put(ProcessorProxy.TEST_MODE_HEADER, true);
  }
}
