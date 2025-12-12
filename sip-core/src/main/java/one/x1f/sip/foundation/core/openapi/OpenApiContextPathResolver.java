package one.x1f.sip.foundation.core.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@AllArgsConstructor
public class OpenApiContextPathResolver {

  private final Optional<OpenAPI> camelRestDSLOpenApi;
  private final CamelContext camelContext;

  @EventListener(ApplicationReadyEvent.class)
  void resolveCamelContextPathInOpenApi() {
    if (camelRestDSLOpenApi.isEmpty()) return;

    String contextPath = camelContext.getRestConfiguration().getContextPath();
    Paths paths = new Paths();
    OpenAPI openAPICamelConfig = camelRestDSLOpenApi.get();
    if (openAPICamelConfig.getPaths() != null && contextPath != null) {
      openAPICamelConfig
          .getPaths()
          .forEach(
              (path, pathItem) ->
                  paths.put(
                      contextPath.concat(StringUtils.isNotEmpty(path) ? path : ""), pathItem));
      openAPICamelConfig.setPaths(paths);
    }
  }
}
