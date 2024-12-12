package de.ikor.sip.foundation.core.declarative.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteInfo {
  String routeId;
  String routeRole;
}
