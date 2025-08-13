package one.x1f.sip.foundation.core.declarative.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteDeclarativeStructureInfo {
  String connectorGroupId;
  String scenarioId;
  String connectorId;
}
