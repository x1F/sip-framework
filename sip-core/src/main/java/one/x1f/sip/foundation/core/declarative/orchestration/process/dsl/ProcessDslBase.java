package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.DslDefinitionBase;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;

/**
 * Base element for DSL classes used to orchestrate in process orchestration
 *
 * @param <R> DSL handle for the return DSL Verb/type.
 * @param <S> DSL handle for self - element that extends the base
 */
public abstract class ProcessDslBase<S extends ProcessDslBase<S, R>, R>
    extends DslDefinitionBase<S, R> {

  @Getter(AccessLevel.PACKAGE)
  @JsonIgnore
  private final CompositeProcessDefinition compositeProcess;

  ProcessDslBase(final R dslReturnDefinition, final CompositeProcessDefinition compositeProcess) {
    super(dslReturnDefinition);
    this.compositeProcess = compositeProcess;
  }
}
