package one.x1f.sip.foundation.core.declarative;

import java.util.*;
import one.x1f.sip.foundation.core.declarative.dto.ProcessorInfo;
import one.x1f.sip.foundation.core.declarative.dto.ProcessorType;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.stereotype.Service;

/**
 * Internal registry that holds mapping between connectors and their extensions/processors on
 * routes.
 *
 * <p><em>For internal use only</em>
 */
@Service
public class ConnectorRegistry {

  private final MultiValuedMap<String, ProcessorInfo> processorExtensionsForConnectors =
      new HashSetValuedHashMap<>();

  /**
   * Register a connector extension or processor
   *
   * @param routeId id of the route where the extension/processor is located
   * @param extensionId if of the extension/processor
   * @param order order when it's used on the route
   * @param label label of the extension/processor
   * @param uri raw uri of the processor/extension
   * @param type {@link ProcessorType}
   */
  public void registerProcessorExtension(
      String routeId, String extensionId, int order, String label, String uri, ProcessorType type) {
    var processorInfo =
        ProcessorInfo.builder()
            .order(order)
            .id(extensionId)
            .label(removeSuffix(label))
            .uri(uri)
            .type(type)
            .build();
    processorExtensionsForConnectors.put(routeId, processorInfo);
  }

  /**
   * Find extensions/processors on a route
   *
   * @param routeId id of the route
   * @return collection of {@link ProcessorInfo}
   */
  public Collection<ProcessorInfo> getProcessorExtensions(String routeId) {
    return processorExtensionsForConnectors.get(routeId);
  }

  private String removeSuffix(String label) {

    int indexQuestion = label.indexOf("?");

    if (indexQuestion != -1) {
      return label.substring(0, indexQuestion);
    }
    return label;
  }
}
