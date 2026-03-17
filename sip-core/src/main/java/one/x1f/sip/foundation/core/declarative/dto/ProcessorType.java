package one.x1f.sip.foundation.core.declarative.dto;

/** Represents type of the processor or extension */
public enum ProcessorType {
  REQUEST,
  RESPONSE,
  ENTRY,
  EXIT,
  MARSHALLER,
  UNMARSHALLER,
  SCENARIO_HANDOFF,
  SCENARIO_TAKEOVER
}
