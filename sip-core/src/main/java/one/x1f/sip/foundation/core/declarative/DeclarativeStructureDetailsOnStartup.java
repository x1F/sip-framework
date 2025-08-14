package one.x1f.sip.foundation.core.declarative;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DeclarativeStructureDetailsOnStartup {

  private final DeclarationsRegistry declarationsRegistry;

  @EventListener(ApplicationReadyEvent.class)
  public void logDeclarativeStructureDetails() {
    log.info("SIP Framework Status:");
    logElements("Connectors", declarationsRegistry.getConnectors());
    logElements("Integration Scenarios", declarationsRegistry.getScenarios());
    logElements("Composite Processes", declarationsRegistry.getProcesses());
    logElements("Connector Groups", declarationsRegistry.getConnectorGroups());
    log.info("SIP Framework - Adapter started and ready");
  }

  private static <T extends DeclarativeElement> void logElements(
      String elementName, List<T> elements) {
    log.info(
        "{} ({}): {}",
        elementName,
        elements.size(),
        elements.stream().map(DeclarativeElement::getId).collect(Collectors.joining(", ")));
  }
}
