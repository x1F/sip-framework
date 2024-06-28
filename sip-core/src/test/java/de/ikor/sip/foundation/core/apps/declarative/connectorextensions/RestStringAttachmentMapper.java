package de.ikor.sip.foundation.core.apps.declarative.connectorextensions;

import de.ikor.sip.foundation.core.declarative.model.ModelMapper;

public class RestStringAttachmentMapper implements ModelMapper<String, String> {

  public static final String STRING_ATTACHEMENT = "RestStringAttachmentMapper";

  @Override
  public String mapToTargetModel(String sourceModel) {
    return sourceModel + " " + STRING_ATTACHEMENT;
  }
}
