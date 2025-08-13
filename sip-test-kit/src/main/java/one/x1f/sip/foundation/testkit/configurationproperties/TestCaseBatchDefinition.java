package one.x1f.sip.foundation.testkit.configurationproperties;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.config.AutoBatchTestCaseLoading;
import one.x1f.sip.foundation.testkit.util.TestCaseDefinitionValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/** Batch test definitions with syntax validation. */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
@ConditionalOnBean(AutoBatchTestCaseLoading.class)
public class TestCaseBatchDefinition implements Validator {

  static final String DUPLICATE_TEST_TITLE_MESSAGE =
      "Non unique test case titles detected: [\"%s\"]. Please assign a unique title to each test case.";

  @Valid private List<TestCaseDefinition> testCaseDefinitions = new ArrayList<>();

  @Override
  public boolean supports(Class<?> clazz) {
    return TestCaseBatchDefinition.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    TestCaseBatchDefinition testCaseBatchDefinition = (TestCaseBatchDefinition) target;
    for (TestCaseDefinition definition : testCaseBatchDefinition.getTestCaseDefinitions()) {
      TestCaseDefinitionValidator.validate(definition);
    }

    validateUniqueTestTitles(testCaseBatchDefinition);
  }

  private static void validateUniqueTestTitles(TestCaseBatchDefinition testCaseBatchDefinition) {
    List<String> duplicateCases =
        testCaseBatchDefinition.getTestCaseDefinitions().stream()
            .map(TestCaseDefinition::getTitle)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .filter(m -> m.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
    if (!duplicateCases.isEmpty()) {
      throw SIPFrameworkException.init(
          DUPLICATE_TEST_TITLE_MESSAGE, StringUtils.join(duplicateCases, "\",\""));
    }
  }
}
