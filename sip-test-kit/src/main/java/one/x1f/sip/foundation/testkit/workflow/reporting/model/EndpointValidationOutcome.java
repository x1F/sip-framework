package one.x1f.sip.foundation.testkit.workflow.reporting.model;

import lombok.Getter;

public enum EndpointValidationOutcome {
  SUCCESSFUL("successful"),
  UNSUCCESSFUL("unsuccessful"),
  SKIPPED("skipped");

  @Getter private String name;

  EndpointValidationOutcome(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
