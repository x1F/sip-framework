package one.x1f.sip.foundation.core.proxies.extension;

import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import org.apache.camel.Exchange;

/** Proxy extension API */
public interface ProxyExtension {

  /**
   * Method every extension should implement. Called when extension detected
   *
   * @param proxy {@link ProcessorProxy}
   * @param original Original Exchange
   * @param current Currently modified Exchange
   */
  void run(ProcessorProxy proxy, Exchange original, Exchange current);

  /**
   * Check whether this ProxyExtension should be used
   *
   * @param proxy {@link ProcessorProxy}
   * @param original {@link Exchange} before processing
   * @param current {@link Exchange} after processing
   * @return true if this ProxyExtension is applicable
   */
  boolean isApplicable(ProcessorProxy proxy, Exchange original, Exchange current);
}
