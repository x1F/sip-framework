package de.ikor.sip.foundation.core.declarative.connectorgroup;

import de.ikor.sip.foundation.core.declarative.DeclarativeElement;
import de.ikor.sip.foundation.core.declarative.annotation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annotation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.ConnectorGroup;

/**
 * Interface defining a connector group.
 *
 * <p>A connector group combines a set of connectors which are part of one logical unit, such as an
 * external system, or multiple external systems forming one unit.
 *
 * <p>If an {@link InboundConnector} and an {@link OutboundConnector} belong to the same connector
 * group and are linked to the same {@link IntegrationScenario}, the outbound connector is (in the
 * default configuration) ignored when receiving an integration call from the same groups inbound
 * connector.
 *
 * <p><em>Adapter developers should not implement this interface directly, but extend {@link
 * ConnectorGroupBase} instead.</em>
 *
 * @see ConnectorGroupBase
 * @see ConnectorGroup
 */
public interface ConnectorGroupDefinition extends DeclarativeElement {}
