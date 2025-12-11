package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators;

import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A comparator for comparing JSON strings, based on Jackson object mapper.
 *
 * <p>This comparator converts JSON strings into maps and then compares the maps for equality. The
 * comparison result provides information about whether the two JSON strings are equal and a
 * description of the differences if they are not.
 */
@Slf4j
@Component
public class JsonComparator implements StringComparator {
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Compares two JSON strings for equality.
   *
   * @param expected The expected JSON string.
   * @param actual The actual JSON string to be compared against the expected.
   * @return A {@link ComparatorResult} containing the result of the comparison.
   */
  public ComparatorResult compare(String expected, String actual) {
    try {
      return doCompare(expected, actual);
    } catch (Exception e) {
      throw new IncompatibleStringComparator();
    }
  }

  private ComparatorResult doCompare(String expected, String actual)
      throws JsonProcessingException {
    log.trace(format("Comparing .%n Expected: %s %n Actual: %s ", expected, actual));
    TypeReference<HashMap<String, Object>> type = new TypeReference<>() {};

    Map<String, Object> expectedAsMap = mapper.readValue(expected, type);
    Map<String, Object> actualAsMap = mapper.readValue(actual, type);

    MapDifference<String, Object> difference = Maps.difference(expectedAsMap, actualAsMap);
    return new ComparatorResult(
        difference.areEqual(), difference.areEqual() ? null : difference.toString());
  }
}
