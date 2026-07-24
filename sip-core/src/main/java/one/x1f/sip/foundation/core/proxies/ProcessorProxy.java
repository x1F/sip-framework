package one.x1f.sip.foundation.core.proxies;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.Getter;
import lombok.SneakyThrows;
import one.x1f.sip.foundation.core.proxies.extension.ProxyExtension;
import one.x1f.sip.foundation.core.util.CamelHelper;
import one.x1f.sip.foundation.core.util.CamelProcessorsHelper;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.spi.RouteIdAware;
import org.apache.camel.support.AsyncProcessorSupport;
import org.apache.camel.support.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Proxy for Apache Camel Processors */
public class ProcessorProxy extends AsyncProcessorSupport {
  private static final Logger logger = LoggerFactory.getLogger(ProcessorProxy.class);
  public static final String TEST_MODE_HEADER = "_SipTestMode";
  public static final String TEST_NAME_HEADER = "_SipTestName";
  public static final String MOCK_IGNORE_LIST = "_SipMockIgnoreList";
  public static final String TEST_MODE_PREDICATE = "_SipTestModePredicate";
  public static final UnaryOperator<Exchange> DEFAULT_MOCK_FUNCTION = exchange -> exchange;
  private final NamedNode nodeDefinition;
  private final Processor wrappedProcessor;
  // Processor can already be wrapped by Camel so we unwrap it and store it here
  @Getter private final Processor originalProcessor;
  private final List<ProxyExtension> extensions;
  private List<UnaryOperator<Exchange>> mockFunction;
  private Iterator<UnaryOperator<Exchange>> mockCycle;
  @Getter private final boolean endpointProcessor;
  @Getter private final Class<? extends Processor> type;

  /**
   * Creates new instance of ProcessorProxy
   *
   * @param nodeDefinition {@link NamedNode}
   * @param wrappedProcessor target {@link Processor}
   * @param extensions List of {@link ProxyExtension}
   */
  public ProcessorProxy(
      NamedNode nodeDefinition, Processor wrappedProcessor, List<ProxyExtension> extensions) {
    this.nodeDefinition = nodeDefinition;
    this.wrappedProcessor = wrappedProcessor;
    this.originalProcessor = CamelHelper.unwrapProcessor(wrappedProcessor);
    this.type = this.originalProcessor != null ? this.originalProcessor.getClass() : null;
    this.extensions = new ArrayList<>(extensions);
    this.mockFunction = new ArrayList<>();
    this.endpointProcessor = determineEndpointProcessor();
  }

  /** Resets the state of the proxy to default. */
  public synchronized void reset() {
    mockFunction = new ArrayList<>();
    mockCycle = null;
  }

  public synchronized void initDefaultMock() {
    mock(DEFAULT_MOCK_FUNCTION);
  }

  /**
   * Sets proxy's mock function. In this mode, it will simply return the result of invoking of the
   * exchangeFunction.
   *
   * @param exchangeFunction callback function for mock behavior
   */
  public synchronized void mock(UnaryOperator<Exchange> exchangeFunction) {
    if (this.mockFunction == null) {
      this.mockFunction = new ArrayList<>();
    }
    this.mockFunction.remove(DEFAULT_MOCK_FUNCTION);
    this.mockFunction.add(exchangeFunction);
  }

  /**
   * Add new ProxyExtension for this ProcessorProxy
   *
   * @param proxyExtension {@link ProxyExtension}
   */
  public synchronized void addExtension(ProxyExtension proxyExtension) {
    this.extensions.add(proxyExtension);
  }

  /**
   * @return true if this is a processor that outputs to Endpoint
   */
  private boolean determineEndpointProcessor() {
    return CamelProcessorsHelper.isEndpointProcessor(originalProcessor);
  }

  @Override
  public boolean process(Exchange exchange, AsyncCallback callback) {
    if (isTestMode(exchange) && exchange.getProperty(TEST_MODE_HEADER) == null) {
      exchange.setProperty(TEST_MODE_HEADER, "true");
      exchange.setProperty(TEST_NAME_HEADER, exchange.getMessage().getHeader(TEST_NAME_HEADER));
      exchange.setProperty(MOCK_IGNORE_LIST, exchange.getMessage().getHeader(MOCK_IGNORE_LIST));
    }

    if (nodeDefinition instanceof ChoiceDefinition && isTestMode(exchange)) {
      Predicate<Exchange> predicate =
          e ->
              originalProcessor instanceof RouteIdAware idAware
                  && shouldNotSkipMock(e, idAware.getRouteId());
      exchange.setProperty(TEST_MODE_PREDICATE, predicate);
    }

    Exchange originalExchange = exchange.copy();
    if (isTestMode(exchange)
        && hasMockFunction()
        && shouldNotSkipMock(exchange, nodeDefinition.getId())) {
      mockProcessing(exchange);
    } else {
      processExchange(exchange);
    }

    for (ProxyExtension extension : extensions) {
      if (extension.isApplicable(this, originalExchange, exchange)) {
        extension.run(this, originalExchange, exchange);
      }
    }

    callback.done(true);
    if (exchange.getProperty(TEST_MODE_PREDICATE) != null) {
      exchange.removeProperty(TEST_MODE_PREDICATE);
    }
    return true;
  }

  public String getId() {
    return this.nodeDefinition.getId();
  }

  private boolean isTestMode(Exchange exchange) {
    return "true".equals(exchange.getProperty(TEST_MODE_HEADER, String.class))
        || "true".equals(exchange.getMessage().getHeader(TEST_MODE_HEADER, String.class));
  }

  @SneakyThrows
  private void processExchange(Exchange exchange) {
    logger.trace("Processor: {}, Executing routing logic for the {}", getId(), exchange);
    wrappedProcessor.process(exchange);
  }

  private boolean hasMockFunction() {
    return !this.mockFunction.isEmpty();
  }

  private void mockProcessing(Exchange exchange) {
    logger.trace("Processor: {}, Executing mocking logic for the {} ", getId(), exchange);
    if (!mockFunction.isEmpty() && mockCycle == null) {
      mockCycle = Iterables.cycle(mockFunction).iterator();
    }
    ExchangeHelper.copyResults(exchange, mockCycle.next().apply(exchange));
  }

  private boolean shouldNotSkipMock(Exchange exchange, String id) {
    List<String> mockIgnoreList = exchange.getProperty(MOCK_IGNORE_LIST, List.class);
    return mockIgnoreList == null
        || mockIgnoreList.isEmpty()
        || mockIgnoreList.stream().noneMatch(id::contains);
  }
}
