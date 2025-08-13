package one.x1f.sip.foundation.core.declarative.connector;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumeration for representing the type of endpoint. */
@AllArgsConstructor
public enum ConnectorType {
  IN("in"),

  OUT("out");

  @Getter private final String value;
}
