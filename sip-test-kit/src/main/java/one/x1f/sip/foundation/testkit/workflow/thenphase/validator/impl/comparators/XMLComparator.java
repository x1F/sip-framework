package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators.util.SilentDocumentFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

/**
 * A comparator for comparing XML strings, based on xmlunit library.
 *
 * <p>This comparator uses the DiffBuilder to compare two XML strings for similarity. The comparison
 * result provides information about whether the two XML strings are similar and a description of
 * the differences if they are not.
 */
@Slf4j
public class XMLComparator implements StringComparator {

  /**
   * Compares two XML strings for similarity.
   *
   * @param expected The expected XML string.
   * @param actual The actual XML string to be compared against the expected.
   * @return A {@link ComparatorResult} containing the result of the comparison.
   */
  public ComparatorResult compare(String expected, String actual) {
    try {
      return doCompare(expected, actual);
    } catch (Exception e) {
      throw new IncompatibleStringComparator();
    }
  }

  private ComparatorResult doCompare(String expected, String actual) {
    log.trace(format("Comparing xml content.%n Expected: %s %n Actual: %s ", expected, actual));
    Diff diff =
        DiffBuilder.compare(expected)
            .withDocumentBuilderFactory(new SilentDocumentFactory())
            .withTest(actual)
            .normalizeWhitespace()
            .checkForSimilar()
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .build();

    return new ComparatorResult(
        !diff.hasDifferences(), !diff.hasDifferences() ? null : diff.fullDescription());
  }
}
