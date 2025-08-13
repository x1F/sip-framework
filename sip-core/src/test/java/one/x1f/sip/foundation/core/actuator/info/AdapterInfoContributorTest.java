package one.x1f.sip.foundation.core.actuator.info;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

class AdapterInfoContributorTest {

  private static final String BUILD_KEY = "build";
  private static final String ADAPTER_NAME_DETAILS_KEY = "adapter-name";
  private static final String ADAPTER_NAME_BUILD_KEY = "name";
  private static final String ADAPTER_VERSION_DETAILS_KEY = "adapter-version";
  private static final String ADAPTER_VERSION_BUILD_KEY = "version";
  private static final String SIP_FRAMEWORK_VERSION_DETAILS_KEY = "sip-framework-version";
  private static final String SIP_FRAMEWORK_VERSION_BUILD_KEY = "sipFrameworkVersion";

  private static final String ADAPTER_NAME_TEST = "testName";
  private static final String ADAPTER_VERSION_TEST = "1.0.2";
  private static final String SIP_FRAMEWORK_VERSION_TEST = "1.0.1";

  private AdapterInfoContributor subject;
  private Info.Builder builder;

  @BeforeEach
  void setup() {
    subject = new AdapterInfoContributor();
    builder = new Info.Builder();
  }

  @Test
  void Given_buildInfo_When_contribute_Then_returnBasicAdapterInfo() {
    // arrange
    builder.withDetail(BUILD_KEY, createBuildInfo());

    // act
    subject.contribute(builder);

    // assert
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> target =
        (LinkedHashMap<String, Object>) builder.build().get(BUILD_KEY, LinkedHashMap.class);
    assertThat(target)
        .hasSize(3)
        .containsEntry(ADAPTER_NAME_DETAILS_KEY, ADAPTER_NAME_TEST)
        .containsEntry(ADAPTER_VERSION_DETAILS_KEY, ADAPTER_VERSION_TEST)
        .containsEntry(SIP_FRAMEWORK_VERSION_DETAILS_KEY, SIP_FRAMEWORK_VERSION_TEST);
  }

  @Test
  void Given_nullBuildInfo_When_contribute_Then_returnNoBasicAdapterInfo() {
    // arrange
    builder.withDetail(BUILD_KEY, null);

    // act
    subject.contribute(builder);

    // assert
    @SuppressWarnings("unchecked")
    LinkedHashMap<Object, Object> target = builder.build().get(BUILD_KEY, LinkedHashMap.class);
    assertThat(target).isNull();
  }

  private LinkedHashMap<String, Object> createBuildInfo() {
    LinkedHashMap<String, Object> buildInfo = new LinkedHashMap<>();
    buildInfo.put(ADAPTER_NAME_BUILD_KEY, ADAPTER_NAME_TEST);
    buildInfo.put(ADAPTER_VERSION_BUILD_KEY, ADAPTER_VERSION_TEST);
    buildInfo.put(SIP_FRAMEWORK_VERSION_BUILD_KEY, SIP_FRAMEWORK_VERSION_TEST);
    return buildInfo;
  }
}
