package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparatorResult {
  private Boolean status;
  private String failureDescription;
}
