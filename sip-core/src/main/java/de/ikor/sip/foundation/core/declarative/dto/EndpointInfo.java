package de.ikor.sip.foundation.core.declarative.dto;

import lombok.Builder;
import lombok.Value;

/** Class which represents POJO model for exposing Camel endpoint with its route id. */
@Value
@Builder
public class EndpointInfo {
  String endpointId;
  String camelEndpointUri;
  Boolean primary;
}
