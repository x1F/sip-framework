package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators;

import one.x1f.sip.foundation.testkit.util.RegexUtil;

public class RegexComparator implements StringComparator {
  @Override
  public ComparatorResult compare(String expected, String actual) {
    boolean matches = RegexUtil.compare(expected, actual);
    return new ComparatorResult(matches, null);
  }
}
