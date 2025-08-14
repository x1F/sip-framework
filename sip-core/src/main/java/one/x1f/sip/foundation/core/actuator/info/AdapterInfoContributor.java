package one.x1f.sip.foundation.core.actuator.info;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link AdapterInfoContributor} extends {@link InfoContributor} to add adapter information to the
 * info actuator
 */
@Slf4j
@Component
@Order(1)
public class AdapterInfoContributor implements InfoContributor {

  private static final String BUILD_KEY = "build";
  private static final String ADAPTER_NAME_DETAILS_KEY = "adapter-name";
  private static final String ADAPTER_NAME_BUILD_KEY = "name";
  private static final String ADAPTER_VERSION_DETAILS_KEY = "adapter-version";
  private static final String ADAPTER_VERSION_BUILD_KEY = "version";
  private static final String SIP_FRAMEWORK_VERSION_DETAILS_KEY = "sip-framework-version";
  private static final String SIP_FRAMEWORK_VERSION_BUILD_KEY = "sipFrameworkVersion";

  /**
   * Adding adapter base info in actuator/info page.
   *
   * @param builder Info.Builder of the SpringBoot actuator specification.
   */
  @Override
  public void contribute(Info.Builder builder) {
    @SuppressWarnings("unchecked")
    Map<String, Object> buildInfo = builder.build().get(BUILD_KEY, LinkedHashMap.class);

    if (buildInfo != null) {
      collectAdapterInfo(buildInfo);
    } else {
      log.warn("sip.core.actuator.info.missingbuildinfo");
    }
  }

  private void collectAdapterInfo(Map<String, Object> buildInfo) {
    String adapterName = (String) buildInfo.get(ADAPTER_NAME_BUILD_KEY);
    String adapterVersion = (String) buildInfo.get(ADAPTER_VERSION_BUILD_KEY);
    String sipFrameworkVersion = (String) buildInfo.get(SIP_FRAMEWORK_VERSION_BUILD_KEY);

    buildInfo.clear();

    buildInfo.put(ADAPTER_NAME_DETAILS_KEY, adapterName);
    buildInfo.put(ADAPTER_VERSION_DETAILS_KEY, adapterVersion);
    buildInfo.put(SIP_FRAMEWORK_VERSION_DETAILS_KEY, sipFrameworkVersion);
  }
}
