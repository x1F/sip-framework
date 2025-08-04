package de.ikor.sip.foundation.core.declarative.process;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.ikor.sip.foundation.core.apps.declarative.ProcessOrchestrationAdapter.getPartnerDebtById;
import de.ikor.sip.foundation.core.declarative.annotation.CompositeProcess;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.junit.jupiter.api.Test;

class CompositeProcessBaseTest {

  @CompositeProcess(
      processId = ProcessWithoutOrchestrator.ID,
      consumers = {},
      provider = getPartnerDebtById.class)
  private class ProcessWithoutOrchestrator extends CompositeProcessBase {
    public static final String ID = "processWithoutOrchestratorID";
  }

  @Test
  void WHEN_orchestratorNotDefined_THEN_SIPExceptionOccurs() {
    // arrange
    CompositeProcessBase compositeProcessBase = new ProcessWithoutOrchestrator();

    // act & assert
    assertThatThrownBy(compositeProcessBase::getOrchestrator)
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Orchestration needs to be defined for the process '%s' declared in the class '%s'. Please @Override the getOrchestrator() method.",
            ProcessWithoutOrchestrator.ID, ProcessWithoutOrchestrator.class.getName());
  }
}
