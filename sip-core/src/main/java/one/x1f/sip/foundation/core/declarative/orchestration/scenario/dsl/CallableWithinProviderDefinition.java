package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

/**
 * Marker interface for elements that can be nodes below a provider-definition within the scenario
 * orchestration DSL
 */
sealed interface CallableWithinProviderDefinition
    permits CallScenarioConsumerBaseDefinition,
        ConditionalCallScenarioConsumerDefinition,
        ForLoopCallScenarioConsumerDefinition,
        ProcessCallScenarioConsumerDefinition,
        WhileLoopCallScenarioConsumerDefinition {}
