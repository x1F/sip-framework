package one.x1f.sip.foundation.connectors.con1;

import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;

@OutboundConnector(
    connectorGroup = "test",
    integrationScenario = "test",
    requestModel = Object.class)
public class ValidConnectorFromAbstractParent extends NoAnnotationAbstractConnector {}
