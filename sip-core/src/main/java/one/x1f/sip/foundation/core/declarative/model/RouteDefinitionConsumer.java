package one.x1f.sip.foundation.core.declarative.model;

import java.util.function.Consumer;
import org.apache.camel.model.RouteDefinition;

@FunctionalInterface
public interface RouteDefinitionConsumer extends Consumer<RouteDefinition> {}
