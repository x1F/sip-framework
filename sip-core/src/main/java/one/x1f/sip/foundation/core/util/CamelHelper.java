package one.x1f.sip.foundation.core.util;

import lombok.experimental.UtilityClass;
import org.apache.camel.Processor;
import org.apache.camel.processor.WrapProcessor;

/** Utility class to help with Camel's internals */
@UtilityClass
public class CamelHelper {

  /**
   * If an instance of WrapProcessor is received it will return the unwrapped Processor
   *
   * @param wrappedProcessor - Processor that can be wrapped by Camel in a WrapProcessor class
   * @return originalProcessor - Unwrapped processor
   */
  public static Processor unwrapProcessor(Processor wrappedProcessor) {
    Processor originalProcessor = wrappedProcessor;
    while (originalProcessor instanceof WrapProcessor wrapProcessor) {
      originalProcessor = wrapProcessor.getWrapped();
    }
    return originalProcessor;
  }
}
