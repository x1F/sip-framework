package one.x1f.sip.foundation.core.proxies;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Contains registry of {@link ProcessorProxy}, which can be accessed via {@link
 * org.apache.camel.Processor} id.
 */
@Component
@RequiredArgsConstructor
public class ProcessorProxyRegistry {
  private Map<String, ProcessorProxy> proxies = new ConcurrentHashMap<>();

  /**
   * Registers a new {@link ProcessorProxy}
   *
   * @param processorId id of the original {@link org.apache.camel.Processor}
   * @param processorProxy {@link ProcessorProxy} which should be registered
   */
  public void register(String processorId, ProcessorProxy processorProxy) {
    proxies.put(processorId, processorProxy);
  }

  /**
   * Get {@link ProcessorProxy} from registry
   *
   * @param processorId id of original {@link org.apache.camel.Processor}
   * @return if exists {@link ProcessorProxy}
   */
  public Optional<ProcessorProxy> getProxy(String processorId) {
    return Optional.ofNullable(proxies.get(processorId));
  }

  /**
   * @return map of processorId and {@link ProcessorProxy}
   */
  public Map<String, ProcessorProxy> getProxies() {
    return proxies;
  }
}
