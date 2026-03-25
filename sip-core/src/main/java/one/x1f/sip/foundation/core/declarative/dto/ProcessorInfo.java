package one.x1f.sip.foundation.core.declarative.dto;

import lombok.Builder;
import lombok.Value;

/** Represents a processor or extension that is part of a connector */
@Value
@Builder
public class ProcessorInfo {
  private String label;
  private String id;
  private String uri;
  private int order;
  private ProcessorType type;
}
