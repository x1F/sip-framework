package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators;

import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.CamelBodyValidator;

/**
 * SIP abstraction used by Test Kit. Used to implement different comparators in order to provide
 * more powerful body validation process. Any implementation of StringComparator should be
 * instantiated in {@link CamelBodyValidator} in order to take part in body validation process.
 */
public interface StringComparator {
  ComparatorResult compare(String expected, String actual);
}
